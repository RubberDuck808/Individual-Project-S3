import React from "react";

export default function SocialSection() {
  const leaderboard = [
    { name: "Amina", points: 1420, badge: "🏆" },
    { name: "Leo", points: 1310, badge: "🔥" },
    { name: "Maya", points: 1215, badge: "⚡" },
    { name: "Noah", points: 1180, badge: "🛡️" },
  ];

  const achievements = [
    { title: "Hazard Hero", desc: "10 confirmed reports", icon: "🕯️" },
    { title: "Smooth Operator", desc: "100 safe miles", icon: "🛞" },
    { title: "First Responder", desc: "1st to report an incident", icon: "🚨" },
  ];

  return (
    <section id="social" className="max-w-7xl mx-auto px-6 py-20">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-10 items-center">
        {/* Mock UI */}
        <div className="lg:col-span-7 lg:order-1 order-2">
          <div className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur-xl overflow-hidden">
            <div className="p-6 border-b border-black/10 flex items-center justify-between">
              <div className="font-semibold">Friends + Rankings</div>
              <div className="text-sm text-gray-600">This week</div>
            </div>

            <div className="p-6 grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Leaderboard */}
              <div className="rounded-2xl border border-black/10 bg-white p-5">
                <div className="flex items-center justify-between">
                  <div className="font-semibold">Leaderboard</div>
                  <div className="text-xs text-gray-500">Points</div>
                </div>

                <div className="mt-4 space-y-3">
                  {leaderboard.map((p, idx) => (
                    <div
                      key={p.name}
                      className="flex items-center justify-between rounded-xl border border-black/10 bg-gray-50 px-4 py-3"
                    >
                      <div className="flex items-center gap-3">
                        <div className="w-8 text-sm text-gray-600">
                          #{idx + 1}
                        </div>
                        <div className="font-semibold">{p.name}</div>
                        <div className="text-lg">{p.badge}</div>
                      </div>
                      <div className="font-semibold">{p.points}</div>
                    </div>
                  ))}
                </div>
              </div>

              {/* Achievements */}
              <div className="rounded-2xl border border-black/10 bg-white p-5">
                <div className="font-semibold">Achievements</div>

                <div className="mt-4 space-y-3">
                  {achievements.map((a) => (
                    <div
                      key={a.title}
                      className="rounded-xl border border-black/10 bg-gray-50 p-4"
                    >
                      <div className="flex items-center gap-3">
                        <div className="text-xl">{a.icon}</div>
                        <div>
                          <div className="font-semibold">{a.title}</div>
                          <div className="text-xs text-gray-600">{a.desc}</div>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>

                <button className="mt-4 w-full px-4 py-2 rounded-xl bg-gray-900 text-white text-sm font-semibold hover:bg-black transition">
                  Add friends
                </button>
              </div>
            </div>

            <div className="px-6 pb-6 text-sm text-gray-600">
              Earn points for confirmed reports, safe driving streaks, and
              positive community contributions.
            </div>
          </div>
        </div>

        {/* Copy */}
        <div className="lg:col-span-5 lg:order-2 order-1">
          <div className="inline-flex items-center gap-2 rounded-full border border-black/10 bg-white/70 px-4 py-2 text-sm">
            <span className="h-2 w-2 rounded-full bg-indigo-500" />
            Social (friends, achievements, leaderboards)
          </div>

          <h2 className="mt-5 text-4xl font-bold leading-tight">
            Make driving social — and surprisingly fun.
          </h2>

          <p className="mt-4 text-lg text-gray-600 leading-relaxed">
            Add friends, build streaks, unlock achievements, and climb the
            leaderboards. Tripwire turns community contributions into friendly
            competition.
          </p>

          <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 gap-4">
            {[
              { title: "Friends", desc: "Follow your crew and compare stats." },
              {
                title: "Achievements",
                desc: "Badges for safe driving + contributions.",
              },
              {
                title: "Leaderboards",
                desc: "Rank by safety, reports, and consistency.",
              },
              {
                title: "Clubs (later)",
                desc: "Create groups for cities, teams, and fleets.",
              },
            ].map((f) => (
              <div
                key={f.title}
                className="rounded-3xl border border-black/10 bg-white/70 backdrop-blur-xl p-6"
              >
                <div className="font-semibold">{f.title}</div>
                <div className="mt-2 text-sm text-gray-600">{f.desc}</div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </section>
  );
}
