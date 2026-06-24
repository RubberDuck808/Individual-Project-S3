import React, { useEffect, useState } from "react";
import { getAllAvatars, getAllBackgrounds, createAvatar, createBackground, updateAvatar, updateBackground, deleteAvatar, deleteBackground, deactivateAvatar, deactivateBackground } from "../../api/adminApi";
import { Image, Plus, Edit, Trash2, Eye, EyeOff, Users } from "lucide-react";
import { useToast } from "../../context/ToastContext";

export default function AdminAssetsPage() {
  const toast = useToast();
  const [activeTab, setActiveTab] = useState("avatars"); // "avatars" or "backgrounds"
  const [avatars, setAvatars] = useState([]);
  const [backgrounds, setBackgrounds] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [editingAsset, setEditingAsset] = useState(null);
  const [formData, setFormData] = useState({ name: "", imagePath: "", active: true });

  useEffect(() => {
    fetchAssets();
  }, [activeTab]);

  const fetchAssets = async () => {
    try {
      setLoading(true);
      if (activeTab === "avatars") {
        const data = await getAllAvatars();
        setAvatars(data);
      } else {
        const data = await getAllBackgrounds();
        setBackgrounds(data);
      }
    } catch (err) {
      setError(err.message || "Failed to load assets");
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = async () => {
    try {
      if (activeTab === "avatars") {
        await createAvatar(formData.name, formData.imagePath);
      } else {
        await createBackground(formData.name, formData.imagePath);
      }
      setShowCreateModal(false);
      setFormData({ name: "", imagePath: "", active: true });
      fetchAssets();
    } catch (err) {
      toast.error("Failed to create asset: " + err.message);
    }
  };

  const handleUpdate = async (id) => {
    try {
      if (activeTab === "avatars") {
        await updateAvatar(id, formData.name, formData.imagePath, formData.active);
      } else {
        await updateBackground(id, formData.name, formData.imagePath, formData.active);
      }
      setEditingAsset(null);
      setFormData({ name: "", imagePath: "", active: true });
      fetchAssets();
    } catch (err) {
      toast.error("Failed to update asset: " + err.message);
    }
  };

  const handleDelete = async (id) => {
    if (!confirm("Are you sure you want to delete this asset? This cannot be undone.")) return;
    
    try {
      if (activeTab === "avatars") {
        await deleteAvatar(id);
      } else {
        await deleteBackground(id);
      }
      fetchAssets();
    } catch (err) {
      toast.error("Failed to delete asset: " + err.message);
    }
  };

  const handleToggleActive = async (id, currentActive) => {
    try {
      if (activeTab === "avatars") {
        if (currentActive) {
          await deactivateAvatar(id);
        } else {
          await updateAvatar(id, null, null, true);
        }
      } else if (currentActive) {
        await deactivateBackground(id);
      } else {
        await updateBackground(id, null, null, true);
      }
      fetchAssets();
    } catch (err) {
      toast.error("Failed to update asset: " + err.message);
    }
  };

  const openEditModal = (asset) => {
    setEditingAsset(asset);
    setFormData({
      name: asset.name || "",
      imagePath: asset.imagePath || "",
      active: asset.active ?? true,
    });
  };

  const closeModal = () => {
    setShowCreateModal(false);
    setEditingAsset(null);
    setFormData({ name: "", imagePath: "", active: true });
  };

  const assets = activeTab === "avatars" ? avatars : backgrounds;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-black text-3xl mb-2">Asset Management</h1>
          <p className="text-gray-600">Manage avatars and backgrounds</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="flex items-center gap-2 px-6 py-3 bg-[#00D1FF] text-black font-black rounded-lg hover:bg-[#00B8E6] transition-colors border-2 border-black shadow-[4px_4px_0px_0px_rgba(0,0,0,1)]"
        >
          <Plus size={20} />
          <span>Add {activeTab === "avatars" ? "Avatar" : "Background"}</span>
        </button>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b-2 border-black">
        <button
          onClick={() => setActiveTab("avatars")}
          className={`px-6 py-3 font-black transition-colors ${
            activeTab === "avatars"
              ? "bg-[#00D1FF] text-black border-b-4 border-black"
              : "bg-gray-100 text-gray-600 hover:bg-gray-200"
          }`}
        >
          Avatars
        </button>
        <button
          onClick={() => setActiveTab("backgrounds")}
          className={`px-6 py-3 font-black transition-colors ${
            activeTab === "backgrounds"
              ? "bg-[#00D1FF] text-black border-b-4 border-black"
              : "bg-gray-100 text-gray-600 hover:bg-gray-200"
          }`}
        >
          Backgrounds
        </button>
      </div>

      {error && (
        <div className="bg-red-100 border-2 border-red-500 rounded-lg p-4 text-red-700">
          {error}
        </div>
      )}

      {loading ? (
        <div className="text-center py-12">Loading assets...</div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {assets.map((asset) => (
            <div
              key={asset.id}
              className={`bg-white border-2 border-black rounded-2xl shadow-[8px_8px_0px_0px_rgba(0,0,0,1)] overflow-hidden ${
                asset.active ? "" : "opacity-60"
              }`}
            >
              {/* Image */}
              <div className="relative aspect-video bg-gray-100">
                {asset.url ? (
                  <img
                    src={asset.url}
                    alt={asset.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center">
                    <Image size={48} className="text-gray-400" />
                  </div>
                )}
                {!asset.active && (
                  <div className="absolute top-2 right-2 bg-red-500 text-white px-3 py-1 rounded-lg font-bold text-sm">
                    Inactive
                  </div>
                )}
              </div>

              {/* Info */}
              <div className="p-4">
                <h3 className="font-black text-lg mb-2">{asset.name}</h3>
                <div className="text-sm text-gray-600 mb-4">
                  <div>Path: {asset.imagePath}</div>
                  <div className="flex items-center gap-1 mt-1">
                    <Users size={14} />
                    <span>{asset.usageCount || 0} users</span>
                  </div>
                </div>

                {/* Actions */}
                <div className="flex gap-2">
                  <button
                    onClick={() => handleToggleActive(asset.id, asset.active)}
                    className={`flex-1 px-4 py-2 rounded-lg font-bold border-2 border-black ${
                      asset.active
                        ? "bg-yellow-500 hover:bg-yellow-600"
                        : "bg-green-500 hover:bg-green-600"
                    }`}
                  >
                    {asset.active ? (
                      <>
                        <EyeOff size={16} className="inline mr-1" />
                        Deactivate
                      </>
                    ) : (
                      <>
                        <Eye size={16} className="inline mr-1" />
                        Activate
                      </>
                    )}
                  </button>
                  <button
                    onClick={() => openEditModal(asset)}
                    className="px-4 py-2 bg-blue-500 text-white rounded-lg font-bold hover:bg-blue-600 border-2 border-black"
                  >
                    <Edit size={16} />
                  </button>
                  <button
                    onClick={() => handleDelete(asset.id)}
                    className="px-4 py-2 bg-red-500 text-white rounded-lg font-bold hover:bg-red-600 border-2 border-black"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Create/Edit Modal */}
      {(showCreateModal || editingAsset) && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white border-2 border-black rounded-2xl p-6 max-w-md w-full mx-4 shadow-[8px_8px_0px_0px_rgba(0,0,0,1)]">
            <h2 className="font-black text-2xl mb-4">
              {editingAsset ? "Edit" : "Create"} {activeTab === "avatars" ? "Avatar" : "Background"}
            </h2>
            <div className="space-y-4">
              <div>
                <label htmlFor="asset-name" className="block font-bold mb-2">Name</label>
                <input
                  id="asset-name"
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="w-full border-2 border-black rounded-lg px-4 py-2"
                  placeholder="Asset name"
                />
              </div>
              <div>
                <label htmlFor="asset-image-path" className="block font-bold mb-2">Image Path (Google Cloud Storage)</label>
                <input
                  id="asset-image-path"
                  type="text"
                  value={formData.imagePath}
                  onChange={(e) => setFormData({ ...formData, imagePath: e.target.value })}
                  className="w-full border-2 border-black rounded-lg px-4 py-2"
                  placeholder="e.g., profile/avatars/preset/robot-blue.png"
                />
              </div>
              {editingAsset && (
                <div>
                  <label className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      checked={formData.active}
                      onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                      className="w-4 h-4"
                    />
                    <span className="font-bold">Active</span>
                  </label>
                </div>
              )}
              <div className="flex gap-2">
                <button
                  onClick={editingAsset ? () => handleUpdate(editingAsset.id) : handleCreate}
                  className="flex-1 px-6 py-3 bg-[#00D1FF] text-black font-black rounded-lg hover:bg-[#00B8E6] border-2 border-black"
                >
                  {editingAsset ? "Update" : "Create"}
                </button>
                <button
                  onClick={closeModal}
                  className="px-6 py-3 bg-gray-200 text-black font-black rounded-lg hover:bg-gray-300 border-2 border-black"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
