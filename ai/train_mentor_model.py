import json
import mysql.connector
import pandas as pd
import joblib
from datetime import datetime, timedelta

from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report

DB_HOST = "127.0.0.1"
DB_PORT = 3306
DB_NAME = "hamod"
DB_USER = "root"
DB_PASS = ""

def main():
    cnx = mysql.connector.connect(
        host=DB_HOST, port=DB_PORT, user=DB_USER, password=DB_PASS, database=DB_NAME
    )

    # 1) Build a training dataset per assignment (entrepreneur_id, mentor_id)
    # label = good_match from session_completed + rating
    # features use mentor history + complaints
    query = """
            SELECT
                ma.id AS assignment_id,
                ma.entrepreneur_id,
                ma.mentor_id,
                COALESCE(ma.session_completed, 0) AS session_completed,
                COALESCE(ma.rating, 0) AS rating,

                (SELECT COUNT(*) FROM mentor_assignment x
                 WHERE x.mentor_id = ma.mentor_id) AS mentor_total_sessions,

                (SELECT COUNT(*) FROM mentor_assignment x
                 WHERE x.mentor_id = ma.mentor_id AND COALESCE(x.session_completed,0)=1) AS mentor_completed_sessions,

                (SELECT COALESCE(AVG(x.rating),0) FROM mentor_assignment x
                 WHERE x.mentor_id = ma.mentor_id AND x.rating IS NOT NULL) AS mentor_avg_rating,

                -- mentor complaints last 90d
                (SELECT COUNT(*) FROM reclamations r
                 WHERE r.target_id = ma.mentor_id
                   AND r.title = 'USER_PROBLEM'
                   AND r.created_at >= NOW() - INTERVAL 90 DAY) AS mentor_complaints_90d,

                -- mentor complaints all time
                (SELECT COUNT(*) FROM reclamations r
            WHERE r.target_id = ma.mentor_id
              AND r.title = 'USER_PROBLEM') AS mentor_complaints_all

            FROM mentor_assignment ma
            WHERE ma.mentor_id IS NOT NULL
              AND ma.entrepreneur_id IS NOT NULL \
            """
    df = pd.read_sql(query, cnx)
    cnx.close()

    if df.empty:
        print("No data in mentor_assignment to train on.")
        return

    # 2) Label
    df["good_match"] = ((df["session_completed"] == 1) & (df["rating"] >= 4)).astype(int)

    # 3) More features
    df["mentor_completed_rate"] = df["mentor_completed_sessions"] / df["mentor_total_sessions"].replace(0, 1)

    feature_cols = [
        "mentor_total_sessions",
        "mentor_completed_sessions",
        "mentor_completed_rate",
        "mentor_avg_rating",
        "mentor_complaints_90d",
        "mentor_complaints_all"
    ]
    X = df[feature_cols]
    y = df["good_match"]

    if len(df) < 10:
        print("Not enough rows for proper train/test split. Add more mentor_assignment rows.")
        print("Current rows:", len(df))
        return

    X_train, X_test, y_train, y_test = train_test_split(
        X, y, test_size=0.25, random_state=42, stratify=y
    )

    # 4) Train (explainable model)
    model = LogisticRegression(max_iter=1000)
    model.fit(X_train, y_train)

    # 5) Evaluate
    y_pred = model.predict(X_test)
    print(classification_report(y_test, y_pred))

    # 6) Save model + feature columns
    joblib.dump({"model": model, "features": feature_cols}, "ai/mentor_model.joblib")
    print("Saved: ai/mentor_model.joblib")

if __name__ == "__main__":
    main()