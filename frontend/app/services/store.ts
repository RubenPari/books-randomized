/**
 * WarpDrive / EmberData store configuration using the legacy adapter pattern.
 * Configured with a JSON:API cache. Handlers and schemas can be added as needed.
 */
import { useLegacyStore } from '@warp-drive/legacy';
import { JSONAPICache } from '@warp-drive/json-api';

const Store = useLegacyStore({
  linksMode: false,
  cache: JSONAPICache,
  handlers: [
    // -- your handlers here
  ],
  schemas: [
    // -- your schemas here
  ],
});

type Store = InstanceType<typeof Store>;

export default Store;
