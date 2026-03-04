import os, json, warnings
warnings.filterwarnings("ignore")

import mysql.connector
import pandas as pd
import joblib

from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report

DB_HOST="127.0.0.1"
DB_PORT=3306
DB_NAME="startupflow"
DB_USER="root"
DB_PASS=""

def main():
    cnx = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS, database=DB_NAME
    )

    # One row per mentor with aggregated feedback + complaints
    query = """
            SELECT
                sf.mentorID AS mentor_id,
                COUNT(*) AS feedback_count,
                AVG(sf.progressScore) AS avg_score,
                MIN(sf.progressScore) AS min_score,
                MAX(sf.progressScore) AS max_score,

                (SELECT AVG(x.progressScore)
                 FROM session_feedback x
                 WHERE x.mentorID = sf.mentorID
                   AND x.feedbackDate >= CURDATE() - INTERVAL 90 DAY) AS recent_avg_score,

                (SELECT COUNT(*)
            FROM reclamations r
            WHERE r.target_id = sf.mentorID
              AND r.title='USER_PROBLEM'
              AND r.created_at >= NOW() - INTERVAL 90 DAY) AS complaints_90d,

                (SELECT COUNT(*)
            FROM reclamations r
            WHERE r.target_id = sf.mentorID
              AND r.title='USER_PROBLEM') AS complaints_all

            FROM session_feedback sf
            GROUP BY sf.mentorID
            """
    df = pd.read_sql(query, cnx)
    cnx.close()

    df = df.fillna(0)

    if df.empty or len(df) < 5:
        print("Not enough mentors/feedback to train. Need more rows.")
        return


    df["good_mentor"] = (df["avg_score"] >= 65).astype(int)

    feature_cols = ["feedback_count","avg_score","min_score","max_score","recent_avg_score","complaints_90d","complaints_all"]
    X = df[feature_cols]
    y = df["good_mentor"]

    # Stratify only if both classes have at least 2 samples
    n_pos, n_neg = int(y.sum()), len(y) - int(y.sum())
    use_stratify = n_pos >= 2 and n_neg >= 2
    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.25, random_state=42, stratify=y if use_stratify else None
    )

    model = LogisticRegression(max_iter=1000)
    model.fit(X_train, y_train)

    print(classification_report(y_test, model.predict(X_test)))

    base_dir = os.path.dirname(os.path.abspath(__file__))
    model_path = os.path.join(base_dir, "mentor_model.joblib")
    joblib.dump({"model": model, "features": feature_cols}, model_path)
    print("Saved:", model_path)

if __name__ == "__main__":
    main()