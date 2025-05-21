import { Routes, Route, useNavigate } from "react-router-dom";
import { useEffect } from "react";
import App from "./App";
import Logs from "./components/Logs";
import { useThread } from "./context/ThreadContext";

function RedirectToThread() {
  const { threadId, resetThread } = useThread();
  const navigate = useNavigate();

  useEffect(() => {
    if (!threadId) {
      resetThread();
    }
  }, [threadId, resetThread]);

  useEffect(() => {
    if (threadId) {
      navigate(`/${threadId}/messages`);
    }
  }, [threadId, navigate]);

  if (!threadId) {
    return (
      <div className="loading-container">
        <div className="track">
          <div className="ball" />
        </div>
        <div className="loading-text">Loading messages from thread...</div>
      </div>
    );
  }
}

export default function AppRouter() {
  return (
    <Routes>
      <Route path="/" element={<RedirectToThread />} />
      <Route path="/:threadId/messages" element={<App />} />
      <Route path="/:threadId/run/:runId/logs" element={<Logs />} /> 
    </Routes>
  );
}
