import { createContext, useContext } from "react";

const LocationContext = createContext(null);

export const useLocation = () => useContext(LocationContext);

export default LocationContext;
