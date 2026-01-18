import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/auth";
import { isValidEmail } from "../utils/emailValidation";

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");

  const handleLogin = async (e) => {
    e.preventDefault();
    setError("");

    if (!email && !password) return setError("Enter your credentials, driver!");
    if (!email) return setError("We need your email!");
    if (!isValidEmail(email)) return setError("That email looks a bit funky...");
    if (!password) return setError("Don't forget your password!");

    try {
      const data = await login(email.trim().toLowerCase(), password);
      // Redirect admins to admin panel, regular users to map
      const redirectPath = data.user?.roleName?.toUpperCase() === "ADMIN" ? "/admin" : "/map";
      navigate(redirectPath);
    } catch {
      setError("Wrong email or password. Try again!");
    }
  };

  return (
    <div className="min-h-screen bg-[#FFFDF5] flex flex-col items-center justify-center p-6 selection:bg-[#FFD600]">
      {/* Decorative Background Orbs to match Home */}
      <div className="fixed inset-0 z-0 pointer-events-none overflow-hidden">
        <div className="absolute top-[-10%] right-[-5%] w-[400px] h-[400px] bg-[#00D1FF]/10 blur-[100px] rounded-full" />
        <div className="absolute bottom-[-10%] left-[-5%] w-[400px] h-[400px] bg-[#FF6AC1]/10 blur-[100px] rounded-full" />
      </div>

      <div className="relative z-10 w-full max-w-md">
        {/* Brand Logo / Return Link */}
        <button 
          type="button"
          className="flex justify-center mb-10 cursor-pointer group"
          onClick={() => navigate("/")}
          aria-label="Return to home"
        >
          <div className="w-14 h-14 bg-[#0066FF] border-4 border-black rounded-2xl flex items-center justify-center rotate-[-8deg] group-hover:rotate-0 transition-all shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
            <span className="text-white font-[1000] text-3xl">T</span>
          </div>
        </button>

        {/* The Login Card */}
        <div className="bg-white border-[4px] border-black rounded-[2.5rem] p-8 md:p-10 shadow-[12px_12px_0px_0px_rgba(0,0,0,1)]">
          <h1 className="text-4xl font-[1000] mb-2 text-slate-900 uppercase italic tracking-tighter leading-none">
            Welcome <br /> <span className="text-[#0066FF]">Back!</span>
          </h1>
          <p className="text-slate-500 font-bold text-sm mb-8 uppercase tracking-widest">
            Ready for your next trip?
          </p>

          <form onSubmit={handleLogin} className="flex flex-col gap-5">
            {error && (
              <div className="bg-[#FF6AC1] text-white border-4 border-black rounded-2xl py-3 px-4 font-black text-center text-sm shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] animate-shake">
                {error}
              </div>
            )}

            <div className="space-y-2">
              <label htmlFor="login-email" className="text-[10px] font-[1000] uppercase tracking-[0.2em] text-slate-400 ml-4">Email Address</label>
              <input
                id="login-email"
                type="email"
                placeholder="driver@tripwire.com"
                className="w-full px-6 py-4 bg-slate-50 border-4 border-black rounded-2xl 
                          text-slate-900 font-bold placeholder-slate-300
                          focus:outline-none focus:bg-white focus:shadow-[4px_4px_0px_0px_#00D1FF] transition-all"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="space-y-2">
              <label htmlFor="login-password" className="text-[10px] font-[1000] uppercase tracking-[0.2em] text-slate-400 ml-4">Password</label>
              <input
                id="login-password"
                type="password"
                placeholder="••••••••"
                className="w-full px-6 py-4 bg-slate-50 border-4 border-black rounded-2xl 
                          text-slate-900 font-bold placeholder-slate-300
                          focus:outline-none focus:bg-white focus:shadow-[4px_4px_0px_0px_#FFD600] transition-all"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            <button
              type="submit"
              className="mt-4 bg-[#0066FF] text-white py-5 rounded-2xl border-4 border-black 
                        font-[1000] uppercase tracking-widest shadow-[6px_6px_0px_0px_#0044AA] 
                        hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-[4px_4px_0px_0px_#0044AA] 
                        active:shadow-none active:translate-x-[6px] active:translate-y-[6px] transition-all"
            >
              Initialize Log In →
            </button>
          </form>

          <div className="mt-10 pt-8 border-t-4 border-dashed border-slate-100 flex flex-col items-center gap-4">
             <p className="text-sm font-bold text-slate-500 uppercase tracking-widest">
              New to the crew?
            </p>
            <button
              onClick={() => navigate("/signup")}
              className="px-8 py-3 bg-[#FFD600] border-4 border-black rounded-full font-black text-xs uppercase tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-[4px] hover:translate-y-[4px] transition-all"
            >
              Create Account
            </button>
          </div>
        </div>

        {/* Bottom Footer Info */}
        <p className="text-center mt-8 text-[10px] font-black uppercase tracking-[0.3em] text-slate-400">
          v.02-BETA // Secure Access Point
        </p>
      </div>
    </div>
  );
}