import "zone.js/testing";
import { getTestBed } from "@angular/core/testing";
import {
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting,
} from "@angular/platform-browser-dynamic/testing";

getTestBed().initTestEnvironment(
  BrowserDynamicTestingModule,
  platformBrowserDynamicTesting(),
  { teardown: { destroyAfterEach: true } }
);

const glob = (import.meta as { glob?: (pattern: string) => Record<string, () => void> }).glob;

if (glob) {
  const specs = glob("./**/*.spec.ts");
  Object.values(specs).forEach((load) => load());
}
