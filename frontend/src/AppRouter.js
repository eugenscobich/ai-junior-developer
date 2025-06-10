import {Routes, Route, Link} from "react-router-dom";
import {useEffect, useMemo, useState} from "react";
import App from "./App";
import Logs from "./components/Logs";
import {useThread} from "./context/ThreadContext";
import InputSection from "./components/InputSection";
import {API_BASE, getBasicAuthHeader} from "./config";

function RedirectToThread() {
    const {setThreadFromId} = useThread();
    // const navigate = useNavigate();
    const [inputValue, setInputValue] = useState("");
    const [loading, setLoading] = useState(false);
    const [threads, setThreads] = useState([]);
    const auth = useMemo(() => getBasicAuthHeader(), []);

    const fetchThreads = async () => {
        try {
            const res = await fetch(`${API_BASE}/api/allthreads`, {
                method: "GET",
                headers: {
                    Authorization: auth,
                    "Content-Type": "application/json",
                },
                credentials: "include",
            });
            const data = await res.json();
            setThreads(data.threadList);
        } catch (error) {
            console.error("Error fetching threads:", error);
        }
    };

    useEffect(() => {
        fetchThreads();
        const interval = setInterval(fetchThreads, 5000);
        return () => clearInterval(interval);
    }, [auth]);

    // useEffect(() => {
    //     if (!threadId) {
    //         resetThread();
    //     }
    // }, [threadId, resetThread]);
    //
    // useEffect(() => {
    //     if (threadId) {
    //         navigate(`/${threadId}/messages`);
    //     }
    // }, [threadId, navigate]);

    const handleInputChange = (event) => setInputValue(event.target.value);

    const handleSend = async () => {
        if (loading) return;
        if (!inputValue.trim()) return;

        setLoading(true);
        setInputValue("");

        try {
            const res = await fetch(`${API_BASE}/api/start/thread`, {
                method: "POST",
                headers: {
                    Authorization: auth,
                    "Content-Type": "application/json",
                },
                credentials: "include",
                body: JSON.stringify({
                    prompt: inputValue,
                }),
            });

            const data = await res.text();
            console.log("data: ", data);
            if (data) {
                setThreadFromId(data);
                setInputValue("");
            }
        } catch (error) {
            console.error("Error starting conversation:", error);
        } finally {
            setLoading(false);
        }
    };

    const handleKeyDown = (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            handleSend();
        }
    };

    const getUniqueThreadIds = () => {
        const seen = new Set();
        return threads
            .map(thread => typeof thread === "string" ? thread : thread.threadId)
            .filter(threadId => {
                if (seen.has(threadId)) return false;
                seen.add(threadId);
                return true;
            });
    };

    return (
        <div className="loading-container">
            <div className="track">
                <div className="ball"/>
            </div>
            <div className="loading-text">Start a new conversation</div>
            <InputSection
                inputValue={inputValue}
                onInputChange={handleInputChange}
                onSend={handleSend}
                onKeyDown={handleKeyDown}
                disabled={loading}
            />
            <div className="loading-text" style={{marginTop: "0"}}>
                {threads.length > 0 ? (
                    <ul style={{padding: "0"}}>
                        {getUniqueThreadIds().map((threadId) => (
                            <li className="label" key={threadId}>
                                <Link to={`/${threadId}/messages`}>
                                    Thread {threadId}
                                </Link>
                            </li>
                        ))}
                    </ul>
                ) : (
                    <div className="loading-text">No threads yet.</div>
                )}
            </div>
        </div>
    );
}

export default function AppRouter() {
    return (
        <Routes>
            <Route path="/" element={<RedirectToThread/>}/>
            <Route path="/:threadId/messages" element={<App/>}/>
            <Route path="/:threadId/run/:runId/logs" element={<Logs/>}/>
        </Routes>
    );
}
