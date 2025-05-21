import { createContext, useContext, useEffect, useState } from "react";
import { fetchThreadData } from "../utils";

const ThreadContext = createContext(null);

export function ThreadProvider({ children }) {
  const [threadId, setThreadId] = useState(null);
  const [assistantId, setAssistantId] = useState(null);

  const setThreadFromId = async (id) => {
    setThreadId(id);
    try {
      const data = await fetchThreadData(id);
      if (data?.assistantId) setAssistantId(data.assistantId);
    } catch (error) {
      console.error("Failed to fetch thread data:", error);
    }
  };

  const resetThread = async () => {
    setThreadId(null);
    setAssistantId(null);
    try {
      const data = await fetchThreadData();

      if (data?.threadId) setThreadId(data.threadId);
      if (data?.assistantId) setAssistantId(data.assistantId);
    } catch (error) {
      console.error("Failed to fetch thread data:", error);
    }
  };

  return (
    <ThreadContext.Provider
      value={{ threadId, assistantId, setThreadFromId, resetThread }}
    >
      {children}
    </ThreadContext.Provider>
  );
}

export function useThread() {
  const context = useContext(ThreadContext);
  if (!context) throw new Error("useThread must be used within ThreadProvider");
  return context;
}
