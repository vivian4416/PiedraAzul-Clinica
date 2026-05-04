
export default {
  bootstrap: () => import('./main.server.mjs').then(m => m.default),
  inlineCriticalCss: true,
  baseHref: '/',
  locale: undefined,
  routes: [
  {
    "renderMode": 1,
    "route": "/"
  },
  {
    "renderMode": 1,
    "route": "/clientes"
  },
  {
    "renderMode": 1,
    "route": "/citas"
  },
  {
    "renderMode": 1,
    "route": "/crear-cita"
  },
  {
    "renderMode": 1,
    "route": "/agendar-cita"
  },
  {
    "renderMode": 1,
    "route": "/configuracion"
  },
  {
    "renderMode": 1,
    "redirectTo": "/",
    "route": "/**"
  }
],
  entryPointToBrowserMapping: undefined,
  assets: {
    'index.csr.html': {size: 13182, hash: 'ec021019274392a3c4bd5d28ba17b9cc55698bed5086587e8be97bfab0769714', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: '78b11f62cdea8fade50060692f62d2ae27161f7135aa45574fd6d4a1eb506660', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
