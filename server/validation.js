export class ApiError extends Error {
  constructor(status, code, message) {
    super(message);
    this.status = status;
    this.code = code;
  }
}

function invalid(message) {
  throw new ApiError(400, "VALIDATION_ERROR", message);
}

export function validId(value) {
  if (
    typeof value !== "string" ||
    !/^[A-Za-z0-9_-]{1,128}$/.test(value)
  ) {
    invalid("Choose a valid record ID.");
  }

  return value;
}

function bodyFields(body, allowed) {
  if (!body || typeof body !== "object" || Array.isArray(body)) {
    invalid("Send a JSON object.");
  }

  const keys = Object.keys(body);

  if (!keys.length) {
    invalid("Provide at least one field.");
  }

  if (keys.some(key => !allowed.includes(key))) {
    invalid("The request contains unsupported fields.");
  }
}

function text(value, label, max, required = false) {
  if (typeof value !== "string") {
    invalid(`${label} must be text.`);
  }

  const cleaned = value.trim();

  if (required && !cleaned) {
    invalid(`Enter ${label.toLowerCase()}.`);
  }

  if (cleaned.length > max) {
    invalid(`${label} must be ${max} characters or fewer.`);
  }

  return cleaned;
}

export function subjectInput(body, current = {}) {
  bodyFields(body, ["name", "lecturerName"]);

  const merged = {
    lecturerName: "",
    ...current,
    ...body
  };

  return {
    name: text(merged.name, "Subject name", 80, true),
    lecturerName: text(merged.lecturerName, "Lecturer name", 80)
  };
}

export function taskInput(body, current = {}) {
  bodyFields(body, [
    "subjectId",
    "title",
    "description",
    "dueDate",
    "priority",
    "completed"
  ]);

  const merged = {
    description: "",
    priority: "MEDIUM",
    completed: false,
    ...current,
    ...body
  };

  const dueDate = merged.dueDate;

  if (
    typeof dueDate !== "string" ||
    !/^\d{4}-\d{2}-\d{2}$/.test(dueDate)
  ) {
    invalid("Enter a valid date in YYYY-MM-DD format.");
  }

  const [year, month, day] = dueDate.split("-").map(Number);

  if (
    year < 1900 ||
    year > 2100 ||
    new Date(Date.UTC(year, month - 1, day))
      .toISOString()
      .slice(0, 10) !== dueDate
  ) {
    invalid("Enter a valid calendar date between 1900 and 2100.");
  }

  if (!["HIGH", "MEDIUM", "LOW"].includes(merged.priority)) {
    invalid("Choose High, Medium or Low priority.");
  }

  if (typeof merged.completed !== "boolean") {
    invalid("Completed must be true or false.");
  }

  return {
    subjectId: validId(merged.subjectId),
    title: text(merged.title, "Task title", 120, true),
    description: text(merged.description, "Description", 2000),
    dueDate,
    priority: merged.priority,
    completed: merged.completed
  };
}