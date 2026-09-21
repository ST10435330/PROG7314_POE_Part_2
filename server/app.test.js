import test from "node:test";
import assert from "node:assert/strict";
import { once } from "node:events";
import { createApp } from "./app.js";

async function start(t, dependencies = {}) {
  const auth = {
    verifyIdToken: async () => {
      throw Object.assign(
        new Error("Invalid test token"),
        { code: "auth/invalid-id-token" }
      );
    }
  };

  const app = createApp({
    auth,
    db: {},
    ...dependencies
  });

  const server = app.listen(0, "127.0.0.1");
  await once(server, "listening");

  t.after(() =>
    new Promise(resolve => server.close(resolve))
  );

  return `http://127.0.0.1:${server.address().port}`;
}

test("health endpoint is public", async t => {
  const url = await start(t);
  const response = await fetch(`${url}/api/health`);

  assert.equal(response.status, 200);
  assert.deepEqual(await response.json(), { status: "ok" });
});

test("all data endpoints require authentication", async t => {
  const url = await start(t);

  for (const kind of ["subjects", "tasks"]) {
    for (const method of ["GET", "POST", "PATCH", "DELETE"]) {
      const suffix = ["PATCH", "DELETE"].includes(method)
        ? "/record-1"
        : "";

      const response = await fetch(
        `${url}/api/${kind}${suffix}`,
        { method }
      );

      assert.equal(response.status, 401);
      assert.equal(
        (await response.json()).error.code,
        "UNAUTHENTICATED"
      );
    }
  }
});

test("invalid tokens are rejected", async t => {
  const url = await start(t);

  const response = await fetch(`${url}/api/tasks`, {
    headers: {
      Authorization: "Bearer invalid"
    }
  });

  assert.equal(response.status, 401);
  await response.json();
});

test("record paths use the verified user, not a query parameter", async t => {
  const paths = [];

  // Test doubles only: production uses Firebase Admin verification.
  const auth = {
    verifyIdToken: async token => ({ uid: token })
  };

  const db = {
    collection: name => {
      assert.equal(name, "users");

      return {
        doc: uid => ({
          collection: kind => ({
            get: async () => {
              paths.push(`users/${uid}/${kind}`);
              return { docs: [] };
            }
          })
        })
      };
    }
  };

  const url = await start(t, { auth, db });

  for (const user of ["user-a", "user-b"]) {
    const response = await fetch(
      `${url}/api/subjects?uid=someone-else`,
      {
        headers: {
          Authorization: `Bearer ${user}`
        }
      }
    );

    assert.equal(response.status, 200);
    assert.deepEqual(await response.json(), []);
  }

  assert.deepEqual(paths, [
    "users/user-a/subjects",
    "users/user-b/subjects"
  ]);
});