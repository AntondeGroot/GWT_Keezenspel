import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideApi } from './api';
import { provideRouter } from '@angular/router';

import { routes } from './app.routes';
import { basePath } from './base-path';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(),
    // API base path = the app's mount prefix, so calls go to /keezen/games under a
    // /keezen deploy and /games at the root.
    provideApi(basePath()),
  ],
};
