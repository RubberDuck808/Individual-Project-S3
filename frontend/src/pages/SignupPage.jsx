import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { register } from "../api/auth";
import { isValidEmail } from "../utils/emailValidation";

export default function SignupPage() {
  const navigate = useNavigate();
  const [username, setUsername] = useState("");
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const handleSignup = async (e) => {
    e.preventDefault();
    setError("");

    if (!username || !email || !password || !confirmPassword || !name) {
      return setError("Fill in all the blanks, Scout!");
    }

    if (!isValidEmail(email)) {
      return setError("That email looks a bit wobbly.");
    }

    if (password !== confirmPassword) {
      return setError("Passwords aren't matching up!");
    }

    setLoading(true);

    try {
      await register(username.trim(), email.trim().toLowerCase(), password, name);
      navigate("/login");
    } catch (err) {
      setError(err.message || "The engine stalled. Try again?");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#FFFDF5] flex flex-col items-center justify-center p-6 selection:bg-[#FFD600]">
      {/* Playful Ambient Blobs */}
      <div className="fixed inset-0 z-0 pointer-events-none overflow-hidden">
        <div className="absolute top-[-5%] left-[-10%] w-[500px] h-[500px] bg-[#FF6AC1]/10 blur-[120px] rounded-full animate-pulse" />
        <div className="absolute bottom-[-5%] right-[-10%] w-[600px] h-[600px] bg-[#00D1FF]/10 blur-[150px] rounded-full animate-pulse" />
      </div>

      <div className="relative z-10 w-full max-w-lg">
        {/* Brand Logo */}
        <button
          type="button"
          className="flex justify-center mb-8 cursor-pointer group mx-auto"
          onClick={() => navigate("/")}
          aria-label="Go to home page"
        >
          <div className="w-14 h-14 bg-[#0066FF] border-4 border-black rounded-2xl flex items-center justify-center rotate-[-8deg] group-hover:rotate-0 transition-all shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]">
            <span className="text-white font-[1000] text-3xl">T</span>
          </div>
        </button>

        {/* Signup Card */}
        <div className="bg-white border-[4px] border-black rounded-[3rem] p-8 md:p-12 shadow-[12px_12px_0px_0px_rgba(0,0,0,1)]">
          <div className="text-center mb-10">
            <h1 className="text-4xl md:text-5xl font-[1000] text-slate-900 uppercase italic tracking-tighter leading-none">
              Join the <br /> <span className="text-[#FF6AC1]">Crew.</span>
            </h1>
            <p className="mt-4 text-slate-500 font-bold text-sm uppercase tracking-widest">
              Start your journey with Tripwire
            </p>
          </div>

          <form onSubmit={handleSignup} className="flex flex-col gap-6">
            {error && (
              <div className="bg-[#FF6AC1] text-white border-4 border-black rounded-2xl py-3 px-4 font-black text-center text-sm shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] animate-bounce">
                {error}
              </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label htmlFor="signup-username" className="text-[10px] font-[1000] uppercase tracking-[0.2em] text-slate-400 ml-4">Codename</label>
                <input
                  id="signup-username"
                  type="text"
                  placeholder="SpeedyJoe"
                  className="w-full px-6 py-4 bg-slate-50 border-4 border-black rounded-2xl font-bold focus:outline-none focus:bg-white focus:shadow-[4px_4px_0px_0px_#00D1FF] transition-all"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <label htmlFor="signup-name" className="text-[10px] font-[1000] uppercase tracking-[0.2em] text-slate-400 ml-4">Real Name</label>
                <input
                  id="signup-name"
                  type="text"
                  placeholder="Joe Smith"
                  className="w-full px-6 py-4 bg-slate-50 border-4 border-black rounded-2xl font-bold focus:outline-none focus:bg-white focus:shadow-[4px_4px_0px_0px_#FFD600] transition-all"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                />
              </div>
            </div>

            <div className="space-y-2">
              <label htmlFor="signup-email" className="text-[10px] font-[1000] uppercase tracking-[0.2em] text-slate-400 ml-4">Email</label>
              <input
                id="signup-email"
                type="email"
                placeholder="hello@tripwire.com"
                className="w-full px-6 py-4 bg-slate-50 border-4 border-black rounded-2xl font-bold focus:outline-none focus:bg-white focus:shadow-[4px_4px_0px_0px_#FF6AC1] transition-all"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-2">
                <label htmlFor="signup-password" className="text-[10px] font-[1000] uppercase tracking-[0.2em] text-slate-400 ml-4">Password</label>
                <input
                  id="signup-password"
                  type="password"
                  placeholder="••••••••"
                  className="w-full px-6 py-4 bg-slate-50 border-4 border-black rounded-2xl font-bold focus:outline-none focus:bg-white focus:shadow-[4px_4px_0px_0px_#0066FF] transition-all"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <label htmlFor="signup-confirm-password" className="text-[10px] font-[1000] uppercase tracking-[0.2em] text-slate-400 ml-4">Confirm</label>
                <input
                  id="signup-confirm-password"
                  type="password"
                  placeholder="••••••••"
                  className="w-full px-6 py-4 bg-slate-50 border-4 border-black rounded-2xl font-bold focus:outline-none focus:bg-white focus:shadow-[4px_4px_0px_0px_#0066FF] transition-all"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`mt-4 bg-[#00D1FF] text-black py-5 rounded-[2rem] border-4 border-black 
                        font-[1000] uppercase tracking-widest shadow-[6px_6px_0px_0px_rgba(0,0,0,1)] 
                        hover:translate-x-[2px] hover:translate-y-[2px] hover:shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] 
                        active:shadow-none active:translate-x-[6px] active:translate-y-[6px] transition-all
                        ${loading ? "opacity-70 cursor-not-allowed" : ""}`}
            >
              {loading ? "Ignition Starting..." : "Create Account →"}
            </button>
          </form>

          <div className="mt-12 pt-8 border-t-4 border-dashed border-slate-100 flex flex-col items-center gap-4">
             <p className="text-sm font-bold text-slate-500 uppercase tracking-widest">
              Part of the crew?
            </p>
            <button
              onClick={() => navigate("/login")}
              className="px-10 py-3 bg-[#FFD600] border-4 border-black rounded-full font-black text-xs uppercase tracking-widest shadow-[4px_4px_0px_0px_rgba(0,0,0,1)] hover:shadow-none hover:translate-x-[4px] hover:translate-y-[4px] transition-all"
            >
              Back to Login
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}