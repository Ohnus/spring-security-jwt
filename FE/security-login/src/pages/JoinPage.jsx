import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";

const BACKEND_API_BASE_URL = import.meta.env
  .VITE_BACKEND_API_BASE_URL;

function JoinPage() {
  const nav = useNavigate();

  // 회원가입 변수
  const [isUsernameValid, setIsUsernameValid] = useState(null);
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [nickname, setNickname] = useState("");
  const [email, setEmail] = useState("");

  // 아이디 중복 검증 API
  useEffect(() => {
    const checkUsername = async () => {
      if (username.length < 4) {
        setIsUsernameValid(null);
        return;
      }

      try {
        const res = await fetch(
          `${BACKEND_API_BASE_URL}/users/check-username?username=${username}`,
          {
            method: "GET",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
          }
        );

        if (!res.ok) {
          const errorBody = await res.json();
          console.log(errorBody.status);
          console.log(errorBody.message);
        }

        const data = await res.json();
        setIsUsernameValid(data);
      } catch {
        setIsUsernameValid(null);
      }
    };

    // Cleanup 함수 + 디바운스(Debounce)
    // JoinPage 렌더링 -> usename 입력 -> 리렌더링 -> useEffect 실행 -> setTimeout(checkUsername) 예약 delay1
    // -> 300ms 내에 다시 타이핑 -> 리렌더링 -> 클린업 함수로 delay1 취소 -> useEffect 실행 -> setTimeout(checkUsername) 예약 delay2
    // -> 300ms 동안 타이핑 멈춤 -> delay2 실행 -> checkUsername으로 중복 검증 API 실행
    const delay = setTimeout(checkUsername, 300);
    // 클린업 함수는 같은 useEffect가 다시 실행되기 직전 또는 컴포넌트 언마운트시 실행(현재는 전자)
    return () => clearTimeout(delay);
  }, [username]);

  // 회원가입
  const handleSignup = async (e) => {
    e.preventDefault();

    if (
      username.length < 4 ||
      password.length < 4 ||
      nickname.trim() === "" ||
      email.trim() === ""
    ) {
      return;
    }

    try {
      const res = await fetch(
        `${BACKEND_API_BASE_URL}/users/signup`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          credentials: "include",
          body: JSON.stringify({
            username,
            password,
            nickname,
            email,
          }),
        }
      );

      if (!res.ok) {
        const errorBody = await res.json();
        console.log(errorBody.status);
        console.log(errorBody.message);
        return;
      }
      nav("/login");
    } catch {
      console.log(e);
    }
  };

  return (
    <>
      <div>
        <h2>회원가입</h2>

        <form onSubmit={handleSignup}>
          <label>아이디</label>
          <input
            type="text"
            placeholder="아이디(4자 이상)"
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
            minLength={4}
          />
          {username.length >= 4 && isUsernameValid !== null && (
            <p>
              {isUsernameValid
                ? "이미 사용 중인 아이디입니다."
                : "사용 가능한 아이디입니다."}
            </p>
          )}
          <label>비밀번호</label>
          <input
            type="password"
            placeholder="비밀번호(4자 이상)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={4}
          />
          <label>닉네임</label>
          <input
            type="text"
            placeholder="닉네임"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            required
          />
          <label>이메일</label>
          <input
            type="email"
            placeholder="이메일 주소"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <button type="submit" disabled={isUsernameValid === true}>
            회원 가입
          </button>
        </form>
      </div>
    </>
  );
}

export default JoinPage;
