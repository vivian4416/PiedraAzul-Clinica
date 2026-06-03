
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
    "route": "/ingresar"
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
    'index.csr.html': {size: 13182, hash: 'd51c44bc8efc67630fc36422b415103ccf11cbf29689a56d23d8710ff08614a4', text: () => import('./assets-chunks/index_csr_html.mjs').then(m => m.default)},
    'index.server.html': {size: 8008, hash: 'ae7d20507b50b742760314f279503da185fcc46b742fe11665b4171b9da912d2', text: () => import('./assets-chunks/index_server_html.mjs').then(m => m.default)},
    'styles-U2LAC5OC.css': {size: 9831, hash: 'SbAFj3YhhJY', text: () => import('./assets-chunks/styles-U2LAC5OC_css.mjs').then(m => m.default)}
  },
};
