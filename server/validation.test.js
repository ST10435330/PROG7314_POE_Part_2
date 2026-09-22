import test from "node:test";
import assert from "node:assert/strict";
import {
  ApiError,
  subjectInput,
  taskInput,
  validId
} from "./validation.js";

const validTask = {
  subjectId: "subject-1",
  title: "Study",
  dueDate: "2026-09-23"
};

const invalid = action => assert.throws(
  action,
  error => error instanceof ApiError && error.status === 400
);

test("subject fields are trimmed and lecturer is optional", () => {
  assert.deepEqual(
    subjectInput({ name: " PROG7314 " }),
    { name: "PROG7314", lecturerName: "" }
  );
});

test("subject name is required and length limits apply", () => {
  for (const name of ["", "   ", "a".repeat(81), 123]) {
    invalid(() => subjectInput({ name }));
  }

  invalid(() => subjectInput({
    name: "Valid",
    lecturerName: "a".repeat(81)
  }));

  assert.equal(
    subjectInput({ name: "a".repeat(80) }).name.length,
    80
  );
});

test("task defaults are applied", () => {
  assert.deepEqual(taskInput(validTask), {
    ...validTask,
    description: "",
    priority: "MEDIUM",
    completed: false
  });
});

test("invalid dates are rejected and real leap days are accepted", () => {
  for (const dueDate of [
    "2026-02-30",
    "2026-02-29",
    "2026-13-01",
    "2026-00-01",
    "2026-01-00",
    "21/09/2026",
    "1899-12-31",
    "2101-01-01"
  ]) {
    invalid(() => taskInput({ ...validTask, dueDate }));
  }

  assert.equal(
    taskInput({ ...validTask, dueDate: "2024-02-29" }).dueDate,
    "2024-02-29"
  );
});

test("task title and description limits apply", () => {
  for (const title of ["", " ", "a".repeat(121), null]) {
    invalid(() => taskInput({ ...validTask, title }));
  }

  invalid(() => taskInput({
    ...validTask,
    description: "a".repeat(2001)
  }));

  assert.equal(
    taskInput({ ...validTask, title: "a".repeat(120) }).title.length,
    120
  );
});

test("unsupported fields and invalid value types are rejected", () => {
  for (const extra of [
    { priority: "URGENT" },
    { completed: "false" },
    { uid: "other-user" },
    { taskId: "injected" }
  ]) {
    invalid(() => taskInput({ ...validTask, ...extra }));
  }

  for (const body of [null, [], {}, "text"]) {
    invalid(() => subjectInput(body));
  }
});

test("partial updates preserve other fields and support reopening", () => {
  const current = taskInput(validTask);
  const completed = taskInput({ completed: true }, current);

  assert.deepEqual(completed, {
    ...current,
    completed: true
  });

  assert.deepEqual(
    taskInput({ completed: false }, completed),
    current
  );
});

test("document IDs cannot contain path separators", () => {
  for (const id of ["", "../user", "a/b", null]) {
    invalid(() => validId(id));
  }

  assert.equal(validId("subject-1"), "subject-1");
});