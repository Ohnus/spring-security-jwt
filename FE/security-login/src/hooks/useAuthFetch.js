import { useAuth } from "./useAuth";
import { useNavigate } from "react-router-dom";

const BACKEND_API_BASE_URL = import.meta.env
  .VITE_BACKEND_API_BASE_URL;

export function useAuthFetch() {
  const nav = useNavigate();
  const { accessToken, setAccessToken } = useAuth();

  return async function (url, options = {}) {
    // 1. AccessToken으로 API 요청 fetch(options: method, headers, body)
    let res = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${accessToken}`,
      },
      credentials: "include",
    });

    // 2. 성공이면 그대로 반환
    if (res.ok) {
      console.log("액세스 토큰 만료 아닌 경우 성공");
      return res;
    }

    // 3. AccessToken 만료 시 재발급
    const reissueRes = await fetch(
      `${BACKEND_API_BASE_URL}/auth/reissue`,
      {
        method: "POST",
        credentials: "include",
      }
    );

    // 4. RefreshToken 만료 시 Home으로 이동
    if (!reissueRes.ok) {
      setAccessToken(null);
      alert("로그인이 해제되었습니다. 다시 로그인 해주세요.");
      nav("/");
      return;
    }

    // 5. 새로운 AccessToken 저장
    const data = await reissueRes.json();
    console.log(
      "액세스 토큰 만료 시 재발급 후 다시 API 요청: " +
        data.accessToken
    );
    setAccessToken(data.accessToken);

    // 6. 원래 API 요청 재실행
    res = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        Authorization: `Bearer ${data.accessToken}`,
      },
      credentials: "include",
    });

    return res;
  };
}
