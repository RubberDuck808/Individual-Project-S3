import React, { useEffect, useState } from "react";
import { getAllUsers, updateUserRole, deactivateUser } from "../../api/adminApi";
import { User, Shield, Trash2, Edit } from "lucide-react";
import { Link } from "react-router-dom";

export default function AdminUsersPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [editingRole, setEditingRole] = useState(null);
  const [newRole, setNewRole] = useState("");

  useEffect(() => {
    fetchUsers();
  }, [page]);

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await getAllUsers(page, 20);
      setUsers(response.content || []);
      setTotalPages(response.totalPages || 0);
    } catch (err) {
      setError(err.message || "Failed to load users");
    } finally {
      setLoading(false);
    }
  };

  const handleUpdateRole = async (userId) => {
    try {
      await updateUserRole(userId, newRole);
      setEditingRole(null);
      fetchUsers();
    } catch (err) {
      alert("Failed to update role: " + err.message);
    }
  };

  const handleDeactivate = async (userId) => {
    if (!confirm("Are you sure you want to deactivate this user?")) return;
    
    try {
      await deactivateUser(userId);
      fetchUsers();
    } catch (err) {
      alert("Failed to deactivate user: " + err.message);
    }
  };

  if (loading && users.length === 0) {
    return <div className="text-center py-12">Loading users...</div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-black text-3xl mb-2">User Management</h1>
        <p className="text-gray-600">Manage all users in the system</p>
      </div>

      {error && (
        <div className="bg-red-100 border-2 border-red-500 rounded-lg p-4 text-red-700">
          {error}
        </div>
      )}

      {/* Users Table */}
      <div className="bg-white border-2 border-black rounded-2xl shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-100 border-b-2 border-black">
              <tr>
                <th className="px-6 py-4 text-left font-black">User</th>
                <th className="px-6 py-4 text-left font-black">Email</th>
                <th className="px-6 py-4 text-left font-black">Role</th>
                <th className="px-6 py-4 text-left font-black">Created</th>
                <th className="px-6 py-4 text-left font-black">Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((user) => (
                <tr key={user.id} className="border-b border-gray-200 hover:bg-gray-50">
                  <td className="px-6 py-4">
                    <Link
                      to={`/profile/${user.username}`}
                      className="flex items-center gap-3 hover:text-[#FF6AC1] transition-colors"
                    >
                      <User size={20} />
                      <div>
                        <div className="font-bold">{user.username}</div>
                        <div className="text-sm text-gray-600">{user.name}</div>
                      </div>
                    </Link>
                  </td>
                  <td className="px-6 py-4">{user.email}</td>
                  <td className="px-6 py-4">
                    {editingRole === user.id ? (
                      <div className="flex items-center gap-2">
                        <select
                          value={newRole}
                          onChange={(e) => setNewRole(e.target.value)}
                          className="border-2 border-black rounded-lg px-3 py-1 font-bold"
                        >
                          <option value="USER">USER</option>
                          <option value="ADMIN">ADMIN</option>
                          <option value="MODERATOR">MODERATOR</option>
                        </select>
                        <button
                          onClick={() => handleUpdateRole(user.id)}
                          className="bg-green-500 text-white px-3 py-1 rounded-lg font-bold hover:bg-green-600"
                        >
                          Save
                        </button>
                        <button
                          onClick={() => setEditingRole(null)}
                          className="bg-gray-500 text-white px-3 py-1 rounded-lg font-bold hover:bg-gray-600"
                        >
                          Cancel
                        </button>
                      </div>
                    ) : (
                      <div className="flex items-center gap-2">
                        <Shield size={16} />
                        <span className="font-bold">{user.roleName || "USER"}</span>
                        <button
                          onClick={() => {
                            setEditingRole(user.id);
                            setNewRole(user.roleName || "USER");
                          }}
                          className="ml-2 text-blue-600 hover:text-blue-800"
                        >
                          <Edit size={16} />
                        </button>
                      </div>
                    )}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-600">
                    {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : "N/A"}
                  </td>
                  <td className="px-6 py-4">
                    <button
                      onClick={() => handleDeactivate(user.id)}
                      className="text-red-600 hover:text-red-800"
                      title="Deactivate user"
                    >
                      <Trash2 size={18} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-6 py-4 border-t-2 border-black flex items-center justify-between">
            <button
              onClick={() => setPage(Math.max(0, page - 1))}
              disabled={page === 0}
              className="px-4 py-2 bg-gray-200 rounded-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
            >
              Previous
            </button>
            <span className="font-bold">
              Page {page + 1} of {totalPages}
            </span>
            <button
              onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
              disabled={page >= totalPages - 1}
              className="px-4 py-2 bg-gray-200 rounded-lg font-bold disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-300"
            >
              Next
            </button>
          </div>
        )}
      </div>
    </div>
  );
}
