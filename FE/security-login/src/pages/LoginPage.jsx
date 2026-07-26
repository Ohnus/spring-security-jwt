import { useState, useEffect } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const BACKEND_API_BASE_URL = import.meta.env
  .VITE_BACKEND_API_BASE_URL;

function LoginPage() {
  const nav = useNavigate();
  const [searchParams] = useSearchParams();

  // AccessToken 훅
  const { accessToken, setAccessToken } = useAuth();

  // 자체 서비스 로그인 정보
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  // 자체 서비스 로그인
  const handleLogin = async (e) => {
    e.preventDefault();

    if (username === "" || password === "") {
      return;
    }

    // 로그인 API 요청
    try {
      const res = await fetch(`${BACKEND_API_BASE_URL}/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username, password }),
      });

      if (!res.ok) {
        const errorBody = await res.json();
        console.log(errorBody.status);
        console.log(errorBody.message);
        alert(errorBody.message);
      }

      const resultBody = await res.json();
      console.log(resultBody.status);
      console.log(resultBody.message);
      setAccessToken(resultBody.data.accessToken);
      nav("/");
    } catch {
      console.log(e);
    }
  };

  // 소셜 로그인
  const handleSocialLogin = (provider) => {
    window.location.href = `${BACKEND_API_BASE_URL}/oauth2/authorization/${provider}`;
  };

  useEffect(() => {
    const status = searchParams.get("status");
    const message = searchParams.get("message");

    if (status) {
      console.log(status);
      alert(message);
      searchParams.set("status", null);
      searchParams.set("message", null);
    }
  }, [searchParams]);

  return (
    <div>
      <h2>로그인</h2>
      <form onSubmit={handleLogin}>
        <label>아이디</label>
        <input
          type="text"
          placeholder="아이디"
          value={username}
          onChange={(e) => setUsername(e.target.value)}
          required
        />
        <label>비밀번호</label>
        <input
          type="password"
          placeholder="비밀번호"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <button type="submit">로그인</button>
      </form>

      <div>
        <button
          onClick={() => {
            handleSocialLogin("naver");
          }}
        >
          Naver로 로그인
        </button>
        <button
          onClick={() => {
            handleSocialLogin("google");
          }}
        >
          Google로 로그인
        </button>
      </div>
    </div>
  );
}

export default LoginPage;
