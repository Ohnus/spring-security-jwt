import { useState, useEffect } from "react";
import { AuthContext } from "./AuthContext";

const BACKEND_API_BASE_URL = import.meta.env
  .VITE_BACKEND_API_BASE_URL;

// 첫 접근 시 AuthProvider 렌더링 -> children(ex. HomePage) 렌더링되면서 accessToken(""), loading(true)이 context 통해 전파
// -> 렌더링 끝난 이후에 useEffect 실행되며 init() 토큰 재발급 실행 -> fetch 응답 setAccessToken(data..), setLoading(false)
// -> 상태 변화로 AuthProvider 리렌더링 -> accessToken(data..), loading(false)이 context 통해 children 전파 -> children 리렌더링
export function AuthProvider({ children }) {
  const [accessToken, setAccessToken] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const init = async (e) => {
      try {
        const res = await fetch(
          `${BACKEND_API_BASE_URL}/auth/reissue`,
          {
            method: "POST",
            credentials: "include",
          }
        );

        if (!res.ok) {
          setAccessToken(null);

          const errorBody = await res.json();
          console.log(errorBody.status);
          console.log(errorBody.message);
        }

        const data = await res.json();
        setAccessToken(data.accessToken);
      } catch {
        console.log(e);
        setAccessToken(null);
      } finally {
        setLoading(false);
      }
    };

    init();
  }, []);

  return (
    <AuthContext.Provider
      value={{ accessToken, setAccessToken, loading }}
    >
      {loading ? <div>로딩 중...</div> : children}
    </AuthContext.Provider>
  );
}
