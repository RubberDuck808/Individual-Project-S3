export default function ProfileBackground({ user, children }) {
  const url = user?.backgroundUrl;

  return (
    <div className="relative">
      {url && (
        <div className="absolute inset-x-0 top-0 h-64 md:h-80 rounded-3xl overflow-hidden">
          <img
            src={url}
            alt="profile background"
            className="w-full h-full object-cover"
          />
        </div>
      )}
      <div className="relative pt-48 md:pt-64">
        {children}
      </div>
    </div>
  );
}