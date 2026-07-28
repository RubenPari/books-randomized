import { spawnSync } from 'node:child_process';

const result = spawnSync(
  process.execPath,
  [new URL('../node_modules/@angular/cli/bin/ng.js', import.meta.url).pathname, 'test', '--watch=false'],
  { stdio: 'inherit' },
);

process.exitCode = result.status ?? 1;
