import os, json, warnings
warnings.filterwarnings("ignore")

import mysql.connector
import pandas as pd
import joblib

DB_HOST="127.0.0.1"
DB_PORT=3306
DB_NAME="startupflow"
DB_USER="root"
DB_PASS=""

def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    model_path = os.path.join(base_dir, "mentor_model.joblib")
    bundle = joblib.load(model_path)
    model = bundle["model"]
    features = bundle["features"]

    cnx = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS, database=DB_NAME
    )

    query = """
            SELECT
                u.id,
                u.full_name,
                COALESCE(u.mentor_expertise,'') AS mentor_expertise,

                COALESCE(sf.feedback_count,0) AS feedback_count,
                COALESCE(sf.avg_score,0) AS avg_score,
                COALESCE(sf.min_score,0) AS min_score,
                COALESCE(sf.max_score,0) AS max_score,
                COALESCE(sf.recent_avg_score,0) AS recent_avg_score,

                (SELECT COUNT(*)
                 FROM reclamations r
                 WHERE r.target_id = u.id
                   AND r.title='USER_PROBLEM'
                   AND r.created_at >= NOW() - INTERVAL 90 DAY) AS complaints_90d,

                (SELECT COUNT(*)
            FROM reclamations r
            WHERE r.target_id = u.id
              AND r.title='USER_PROBLEM') AS complaints_all

            FROM users u
                LEFT JOIN (
                SELECT
                mentorID,
                COUNT(*) AS feedback_count,
                AVG(progressScore) AS avg_score,
                MIN(progressScore) AS min_score,
                MAX(progressScore) AS max_score,
                AVG(CASE WHEN feedbackDate >= CURDATE() - INTERVAL 90 DAY THEN progressScore ELSE NULL END) AS recent_avg_score
                FROM session_feedback
                GROUP BY mentorID
                ) sf ON sf.mentorID = u.id

            WHERE u.role='MENTOR' AND u.status='ACTIVE'
            """

    df = pd.read_sql(query, cnx)
    cnx.close()

    df = df.fillna(0)
    if df.empty:
        print("[]")
        return

    X = df[features]
    probs = model.predict_proba(X)[:, 1] * 100.0
    df["ai_rating"] = probs.round(2)
    # Mentors with no feedback are out-of-distribution; show N/A and do not mark as best
    df["has_enough_data"] = df["feedback_count"] >= 1

    out = []
    for _, row in df.iterrows():
        c90 = int(row["complaints_90d"])
        rating = float(row["ai_rating"]) if row["has_enough_data"] else -1.0  # -1 = N/A
        out.append({
            "id": int(row["id"]),
            "fullName": row["full_name"],
            "expertise": row["mentor_expertise"],
            "reclamations90d": c90,
            "ratingPercent": rating,
            "best": False
        })

    out.sort(key=lambda x: (-x["ratingPercent"], x["reclamations90d"]))

    best_count = 0
    for m in out:
        if best_count < 3 and m["ratingPercent"] >= 0:
            m["best"] = True
            best_count += 1

    print(json.dumps(out, ensure_ascii=False))

if __name__ == "__main__":
    main()