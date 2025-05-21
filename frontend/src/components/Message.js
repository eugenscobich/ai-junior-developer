import { Link } from "react-router-dom";
import { useEffect, useState, useMemo } from "react";
import { API_BASE, getBasicAuthHeader } from "../config";

export default function Message({ messages }) {
  const { userMessage, assistantMessages } = messages;
  const threadId = userMessage?.threadId;

  const [runIdTrack, setRunIdTrack] = useState({});
  const auth = useMemo(() => getBasicAuthHeader(), []);

  useEffect(() => {
    const fetchRunIdTrace = () => {
      fetch(`${API_BASE}/api/runIdTrack`, {
        method: "GET",
        headers: {
          Authorization: auth,
          "Content-Type": "application/json",
        },
        credentials: "include",
      })
        .then((res) => res.json())
        .then(setRunIdTrack)
        .catch(console.error);
    };

    fetchRunIdTrace();

    const interval = setInterval(() => {
      fetchRunIdTrace();
    }, 500);

    return () => clearInterval(interval);
  }, [auth]);

  const getRunIdFromPromptMatch = () => {
    if (!userMessage?.value || !runIdTrack) return "";

    for (const [runId, prompt] of Object.entries(runIdTrack)) {
      if (userMessage.value.includes(prompt)) {
        return runId;
      }
    }

    return "";
  };

  const runId = getRunIdFromPromptMatch();

  return (
    <div className="message-block">
      {userMessage && (
        <div className="message user">
          <div className="label">
            <span>User</span>
            <span className="timestamp">
              {new Date(userMessage.createdAt * 1000).toLocaleTimeString()}
            </span>
          </div>
          <div className="bubble">{userMessage.value}</div>
        </div>
      )}

      <div className="message assistant">
        <div className="label">
          {threadId && runId !== "" && (
            <Link to={`/${threadId}/run/${runId}/logs`}>View Logs...</Link>
          )}
        </div>
      </div>

      {assistantMessages.length > 0 ? (
        assistantMessages.map((message, index) => (
          <div key={index} className="message assistant">
            <div className="label">
              <span>Assistant</span>
              <span className="timestamp">
                {new Date(message.createdAt * 1000).toLocaleTimeString()}
              </span>
            </div>
            <div className="bubble">{message.value}</div>
          </div>
        ))
      ) : (
        <div className="message assistant">
          <div className="label">
            <span>Assistant</span>
            <span className="timestamp">[no response yet]</span>
          </div>
          <div className="bubble">[waiting for response]</div>
        </div>
      )}
    </div>
  );
}
