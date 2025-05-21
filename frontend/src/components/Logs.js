import { useParams, useNavigate } from "react-router-dom";
import { useEffect, useState, useMemo } from "react";
import { API_BASE, getBasicAuthHeader } from "../config";

export default function Logs() {
  const { threadId, runId } = useParams();
  const navigate = useNavigate();

  const [logs, setLogs] = useState({});
  const auth = useMemo(() => getBasicAuthHeader(), []);

  useEffect(() => {
    const fetchLogs = () => {
      fetch(`${API_BASE}/api/logs`, {
        method: "GET",
        headers: {
          Authorization: auth,
          "Content-Type": "application/json",
        },
        credentials: "include",
      })
        .then((res) => res.json())
        .then(setLogs)
        .catch(console.error);
    };

    fetchLogs();

    const interval = setInterval(fetchLogs, 500);
    return () => clearInterval(interval);
  }, [auth]);

  const handleBack = () => {
    navigate(`/${threadId}/messages`);
  };

  const filteredLogs = Object.entries(logs || {}).filter(
    ([key]) => key.includes(threadId) && key.includes(runId)
  );

  return (
    <div className="container-logs">
      <div
        className="message-block"
        style={{
          width: "100%",
          boxSizing: "border-box",
          margin: "8rem auto",
        }}
      >
        <div className="message user">
          <div className="label">
            <span>System</span>
            <span className="timestamp">{new Date().toLocaleTimeString()}</span>
          </div>
          <div className="bubble">
            <div>
              <span style={{ fontSize: "1.02rem" }}>Thread id: </span>
              {threadId}
            </div>
            <div>
              <span style={{ fontSize: "1.02rem" }}>Run id: </span>
              {runId}
            </div>
          </div>
        </div>

        <div className="message assistant">
          <div className="label">
            <button
              onClick={handleBack}
              style={{
                backgroundColor: "transparent",
                color: "#e1e1e1",
                border: "none",
                padding: "0",
                cursor: "pointer",
                fontSize: "0.95rem",
                marginTop: "0.3rem",
                fontFamily: "'Poppins', 'Segoe UI', sans-serif",
              }}
            >
              Back to Messages...
            </button>
          </div>
        </div>

        {logs.length === 0 ? (
          <div className="message assistant">
            <div className="label">
              <span>Logs Block</span>
              <span className="timestamp">[empty]</span>
            </div>
            <div className="bubble">No logs available.</div>
          </div>
        ) : (
          filteredLogs.map(([key, entries]) => (
            <div key={key} className="message assistant">
              <div className="label">
                <span>Logs Block</span>
                <span className="timestamp">
                  {new Date().toLocaleTimeString()}
                </span>
              </div>
              <div
                className="bubble logs"
                style={{
                  color: "#e1e1e1;",
                  fontSize: "0.95rem",
                  whiteSpace: "pre",
                  overflowX: "auto",
                  overflowY: "hidden",
                  paddingBottom: "1rem",
                }}
              >
                {entries.join("\n")}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
