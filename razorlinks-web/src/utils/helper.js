import {subDomainList} from "./constant.js";

export const getApps = () => {
    const subDomain = getSubDomain(window.location.hostname);
    const mainApp = subDomainList.find((app) => app.main);
    if (subDomain === "") return mainApp.app;
    const apps = subDomainList.find((app) => subDomain === app.subdomain);
    return apps ? apps.app : mainApp.app;
}

export const getSubDomain = (location) => {
    const locationParts = location.split('.');
    const isLocalhost = locationParts.slice(-1)[0] === 'localhost';
    const sliceTill = isLocalhost ? -1 : -2;
    return locationParts.slice(0, sliceTill).join("");
}
