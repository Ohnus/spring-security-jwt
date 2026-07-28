import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";
import { useAuthFetch } from "../hooks/useAuthFetch";

const BACKEND_API_BASE_URL = import.meta.env
  .VITE_BACKEND_API_BASE_URL;

function MyInfoPage() {
  const nav = useNavigate();
  const [useInfo, setUserInfo] = useState(null);
  const { accessToken } = useAuth();
  const authFetch = useAuthFetch();

  useEffect(() => {
    if (!accessToken) {
      console.log("액세스 토큰 없음");
      nav("/");
      return;
    }

    const fetchUserInfo = async (e) => {
      try {
        const res = await authFetch(
          `${BACKEND_API_BASE_URL}/users/me`,
          {
            method: "GET",
          }
        );

        if (!res.ok) {
          const errorBody = await res.json();
          console.log(errorBody.status);
          console.log(errorBody.message);

          return;
        }

        const resultBody = await res.json();
        console.log(resultBody.data);
        setUserInfo(resultBody.data);
      } catch {
        console.log(e);
      }
    };

    fetchUserInfo();
    // AuthProvider가 loading=true인 동안엔 children을 렌더링 하지 않음.
    // 즉 이 컴포넌트는 accessToken 확정된 이후에만 마운트 되므로 의존성 배열 빈 값.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return (
    <div>
      <h2>유저 정보</h2>
      <p>아이디: {useInfo?.username}</p>
      <p>
        소셜 유무: {useInfo?.isSocial ? "소셜 유저" : "서비스 유저"}
      </p>
      <p>닉네임: {useInfo?.nickname}</p>
      <p>이메일: {useInfo?.email}</p>
    </div>
  );
}

export default MyInfoPage;
