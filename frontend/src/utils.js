import { API_BASE, getBasicAuthHeader } from "./config";

export async function fetchThreadData() {
  const auth = getBasicAuthHeader();
  const response = await fetch(`${API_BASE}/api/threads`, {
    method: "GET",
    headers: {
      Authorization: auth,
      "Content-Type": "application/json",
    },
    credentials: "include",
  });

  if (!response.ok) {
    throw new Error(`Error fetching thread data: ${response.status}`);
  }

  return await response.json();
}
