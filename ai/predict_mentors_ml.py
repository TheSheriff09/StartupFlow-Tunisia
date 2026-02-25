import warnings
warnings.filterwarnings("ignore")
import json
import mysql.connector
import pandas as pd
import joblib


DB_HOST="127.0.0.1"
DB_PORT=3306
DB_NAME="hamod"
DB_USER="root"
DB_PASS=""

def risk_label(cnt):
    if cnt >= 7: return "HIGH"
    if cnt >= 3: return "MID"
    return "LOW"

def main():
    bundle = joblib.load("ai/mentor_model.joblib")
    model = bundle["model"]
    features = bundle["features"]

    cnx = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS, database=DB_NAME
    )

    # One row per mentor with aggregated features
    query = """
            SELECT
                u.id,
                u.full_name,
                COALESCE(u.mentor_expertise,'') AS mentor_expertise,

                (SELECT COUNT(*) FROM mentor_assignment ma
                 WHERE ma.mentor_id = u.id) AS mentor_total_sessions,

                (SELECT COUNT(*) FROM mentor_assignment ma
                 WHERE ma.mentor_id = u.id AND COALESCE(ma.session_completed,0)=1) AS mentor_completed_sessions,

                (SELECT COALESCE(AVG(ma.rating),0) FROM mentor_assignment ma
                 WHERE ma.mentor_id = u.id AND ma.rating IS NOT NULL) AS mentor_avg_rating,

                (SELECT COUNT(*) FROM reclamations r
                 WHERE r.target_id = u.id
                   AND r.title='USER_PROBLEM'
                   AND r.created_at >= NOW() - INTERVAL 90 DAY) AS mentor_complaints_90d,

                (SELECT COUNT(*) FROM reclamations r
            WHERE r.target_id = u.id
              AND r.title='USER_PROBLEM') AS mentor_complaints_all

            FROM users u
            WHERE u.role='MENTOR' AND u.status='ACTIVE' \
            """
    df = pd.read_sql(query, cnx)
    cnx.close()

    if df.empty:
        print("[]")
        return

    df["mentor_completed_rate"] = df["mentor_completed_sessions"] / df["mentor_total_sessions"].replace(0, 1)

    X = df[features]
    probs = model.predict_proba(X)[:, 1] * 100.0
    df["ai_rating"] = probs.round(2)

    out = []
    for _, row in df.iterrows():
        complaints90 = int(row["mentor_complaints_90d"])
        out.append({
            "id": int(row["id"]),
            "fullName": row["full_name"],
            "expertise": row["mentor_expertise"],
            "reclamations90d": complaints90,
            "risk": risk_label(complaints90),
            "ratingPercent": float(row["ai_rating"])
        })

    # Sort best first
    out.sort(key=lambda x: (-x["ratingPercent"], x["reclamations90d"]))

    # Mark BEST top 3 (not HIGH risk)
    best_count = 0
    for m in out:
        if best_count < 3 and m["risk"] != "HIGH":
            m["best"] = True
            best_count += 1
        else:
            m["best"] = False

    print(json.dumps(out, ensure_ascii=False))

if __name__ == "__main__":
    main()