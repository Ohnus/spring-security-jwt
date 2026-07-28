import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const BACKEND_API_BASE_URL = import.meta.env
  .VITE_BACKEND_API_BASE_URL;

function HomePage() {
  const nav = useNavigate();
  // AccessToken 훅
  const { accessToken, setAccessToken } = useAuth();

  console.log(accessToken);

  const logout = async (e) => {
    try {
      const res = await fetch(`${BACKEND_API_BASE_URL}/logout`, {
        method: "POST",
        credentials: "include",
      });

      if (!res.ok) {
        setAccessToken(null);
        throw new Error("ErrorMsg");
      }

      const resultBody = await res.json();
      console.log(resultBody.status);
      alert(resultBody.message);
    } catch {
      console.log(e);
    } finally {
      setAccessToken(null);
    }
  };

  return (
    <div>
      <h2>Home</h2>
      {!accessToken && (
        <>
          <Link to="/join">회원가입 </Link>
          <Link to="/login">로그인 </Link>
        </>
      )}
      <Link to="/my-info">마이페이지 </Link>
      {accessToken && <button onClick={logout}>로그아웃</button>}
    </div>
  );
}

export default HomePage;
