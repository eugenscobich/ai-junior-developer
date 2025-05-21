import ReactDOM from "react-dom/client";
import "./index.css";
import AppRouter from "./AppRouter";
import { BrowserRouter } from "react-router-dom";
import { ThreadProvider } from "./context/ThreadContext";

const root = ReactDOM.createRoot(document.getElementById("root"));
root.render(
  <BrowserRouter>
    <ThreadProvider>
      <AppRouter />
    </ThreadProvider>
  </BrowserRouter>
);
