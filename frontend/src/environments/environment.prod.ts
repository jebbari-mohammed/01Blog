// Production environment configuration
// This is used when building for production with `ng build --configuration production`

export const environment = {
  production: true,
  apiUrl: '/api'  // Relative URL – Nginx proxies /api/ to the backend container
};
