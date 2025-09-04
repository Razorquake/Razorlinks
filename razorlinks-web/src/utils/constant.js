import AppRouter, {SubDomainRouter} from "../AppRouter.jsx";

export const subDomainList = [
    {subdomain: "www", app: AppRouter, main: true},
    {subdomain: "url", app: SubDomainRouter, main: false}
]