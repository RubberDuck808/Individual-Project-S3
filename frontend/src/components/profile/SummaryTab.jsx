import React from "react";
import { RadioTower, Trophy, Users } from "lucide-react";

export default function SummaryTab({ stats, recentActivity, achievements, liveConvoys, convoyInvites }) {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6 mt-6">
      {/* Example: reuse your sections exactly as-is */}
      <div className="bg-white dark:bg-gray-900 rounded-3xl p-6 border border-gray-200 dark:border-gray-800 shadow-sm">
        <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-6 flex items-center gap-2">
          <RadioTower size={20} className="text-blue-500" /> Recent Activity
        </h3>
        {/* render recentActivity like you do now */}
      </div>

      <div className="bg-white dark:bg-gray-900 rounded-3xl p-6 border border-gray-200 dark:border-gray-800 shadow-sm">
        <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-6 flex items-center gap-2">
          <Trophy size={20} className="text-yellow-500" /> Achievements
        </h3>
        {/* render achievements like you do now */}
      </div>

      <div className="bg-white dark:bg-gray-900 rounded-3xl p-6 border border-gray-200 dark:border-gray-800 shadow-sm">
        <h3 className="text-xl font-bold text-gray-900 dark:text-white mb-6 flex items-center gap-2">
          <Users size={20} className="text-purple-500" /> Live Convoys
        </h3>
        {/* render liveConvoys like you do now */}
      </div>

    </div>
  );
}
