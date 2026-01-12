import { ApplicationConfig } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { routes } from './app.routes';
import { provideAnimationsAsync } from '@angular/platform-browser/animations/async';
import { authInterceptor } from './core/interceptors/auth.interceptor';

/**
 * App Configuration - Main application setup
 * 
 * What we're configuring:
 * 1. provideRouter(routes) - Enable routing system
 * 2. provideHttpClient() - Enable HTTP requests to backend
 * 3. withInterceptors([authInterceptor]) - Add JWT token to all requests
 * 4. provideAnimationsAsync() - Enable Angular Material animations
 */

export const appConfig: ApplicationConfig = {
  providers: [
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([authInterceptor])  // Auto-add JWT token to requests
    ),
    provideAnimationsAsync()
  ]
};
