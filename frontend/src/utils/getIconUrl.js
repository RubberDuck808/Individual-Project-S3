export function getIconUrl(fileName) {
  if (!fileName) return null;
  return `/icons/${fileName}`; 
}
