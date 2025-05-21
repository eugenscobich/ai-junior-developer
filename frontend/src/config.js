export const API_BASE = process.env.REACT_APP_API_BASE || "";

export function getBasicAuthHeader() {
  const username = "user";
  const password = "password";

  const token = btoa(`${username}:${password}`);
  const authHeader = `Basic ${token}`;
  return authHeader;
}
