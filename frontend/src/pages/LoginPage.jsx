import { useState } from "react";
import { useNavigate } from "react-router-dom";

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();

    if (!email || !password) {
        alert("Please enter email and password");
        return;
    }

    try {
        const response = await fetch(
          `${import.meta.env.VITE_API_URL}/api/users/login`,
          {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ email, password }),
          }
        );

        if (!response.ok) {
        const message = await response.text();
        throw new Error(message || "Login failed");
        }

        const user = await response.json();
        localStorage.setItem("user", JSON.stringify(user));
        navigate("/");
    } catch (error) {
        alert(error.message);
    }
    };


  return (
  <div className="flex h-screen">
    {/* Left Section */}
    <div className="hidden md:flex w-1/2 bg-black text-white items-center justify-center">
      <img
        src="/your-image.jpg"
        alt="Showcase"
        className="w-3/4 max-h-[80%] object-contain"
      />
    </div>

    {/* Right Section */}
    <div className="flex w-full md:w-1/2 items-center justify-center bg-[#11191f]">
      
      <div className="w-96">
        <h1 className="text-3xl font-bold mb-8 text-gray-100">
          Log into Tripwire
        </h1>

        <form onSubmit={handleLogin} className="flex flex-col gap-4">

          <input
            type="email"
            placeholder="Email"
            className="w-full px-4 py-3 bg-[#1c262b] border border-gray-600 rounded-2xl 
                       text-gray-200 placeholder-gray-400
                       focus:outline-none focus:border-gray-300 transition"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />

          <input
            type="password"
            placeholder="Password"
            className="w-full px-4 py-3 bg-[#1c262b] border border-gray-600 rounded-2xl 
                       text-gray-200 placeholder-gray-400
                       focus:outline-none focus:border-gray-300 transition"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <button
            type="submit"
            className="bg-blue-600 text-white py-3 rounded-2xl hover:bg-blue-700 transition font-semibold"
          >
            Log In
          </button>
        </form>

        <p className="text-center text-sm text-gray-400 mt-6">
          Don’t have an account?{" "}
          <span
            className="text-blue-400 cursor-pointer"
            onClick={() => navigate("/signup")}
          >
            Sign up
          </span>
        </p>
      </div>

    </div>
  </div>
);

}
