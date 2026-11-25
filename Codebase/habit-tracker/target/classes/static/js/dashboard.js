var TIMES = ["All day", "Morning", "Evening"];
var KEY = "habit-maker";

var habits = [];
try { habits = JSON.parse(localStorage.getItem(KEY)) || []; } catch (e) { habits = []; }

function save() {
  try { localStorage.setItem(KEY, JSON.stringify(habits)); } catch (e) {}
}

function buildChips(containerId, group, values, labelFn) {
  var box = document.getElementById(containerId);
  values.forEach(function (v, i) {
    var label = document.createElement("label");
    label.className = "chip";
    label.innerHTML = '<input type="radio" name="' + group + '" value="' + v + '"' + (i === 0 ? " checked" : "") + '><span></span>';
    label.querySelector("span").textContent = labelFn ? labelFn(v) : v;
    //if (group === "icon") label.title = "Icon " + (i + 1);
    box.appendChild(label);
  });
}
buildChips("times", "time", TIMES);
//buildChips("icons", "icon", ICONS);

function iso(d) {
  return d.getFullYear() + "-" + String(d.getMonth() + 1).padStart(2, "0") +
    "-" + String(d.getDate()).padStart(2, "0");
}
var today = new Date(), later = new Date(Date.now() + 30 * 864e5);
document.getElementById("start").value = iso(today);
document.getElementById("end").value = iso(later);

function fmt(dateStr) {
  var d = new Date(dateStr + "T00:00:00");
  return d.toLocaleDateString(undefined, { month: "short", day: "numeric" });
}


 function render() {
  var list = document.getElementById("list");
  list.innerHTML = "";
  if (habits.length === 0) {
    list.innerHTML = '<div class="empty">No habits yet. Fill in the form to create your first one.</div>';
    return;
  }
  habits.forEach(function (h) {
    var el = document.createElement("div");
    el.className = "habit" + (h.id === editingId ? " editing" : "");
    el.dataset.id = h.id;
    el.innerHTML =
      '<div class="info"><strong></strong><small></small></div>' +
      '<div class="actions">' +
        '<button type="button" class="ghost edit">Edit</button>' +
        '<button type="button" class="ghost del">Delete</button>' +
        '<button type="button" class="done">Complete</button>' +
      '</div>';

    el.querySelector("strong").textContent = h.name;
    el.querySelector("small").textContent =
      h.duration + " · " + h.time + " · " + fmt(h.start) + " to " + fmt(h.end);

    el.querySelector(".done").addEventListener("click", function () { completeHabit(h, el); });
    el.querySelector(".edit").addEventListener("click", function () { startEdit(h); });
    var del = el.querySelector(".del");
    del.addEventListener("click", function () {
      if (del.dataset.armed) { deleteHabit(h, el); return; }
      del.dataset.armed = "1";
      del.textContent = "Confirm delete";
      setTimeout(function () { delete del.dataset.armed; del.textContent = "Delete"; }, 3000);
    });

    list.appendChild(el);
  });
}

function removeHabit(id, el) {
  el.classList.add("leaving");
  setTimeout(function () {
    habits = habits.filter(function (h) { 
        return h.id !== id; 
    });
    save();
    if (editingId === id) 
        resetForm(); 
    else render();
  }, 250);
}

function taskPayload(data) {
  return {
    taskName: data.name,
    startDate: data.start,
    endDate: data.end,
    duration: data.duration,
    timeOfDay: data.time
  };
}

async function sendTaskRequest(url, method, body) {
  var response = await fetch(url, {
    method: method,
    headers: { "Content-Type": "application/json" },
    body: body == null ? undefined : JSON.stringify(body)
  });
  if (!response.ok) throw new Error("Unable to save task (" + response.status + ").");
  return method === "POST" ? response.json() : null;
}

async function completeHabit(habit, element) {
  var error = document.getElementById("error");
  error.textContent = "";
  try {
    if (habit.serverId != null) {
      await sendTaskRequest(
        "/api/calendar/tasks/" + encodeURIComponent(habit.serverId) +
          "/completion?date=" + encodeURIComponent(iso(new Date())),
        "PUT",
        { completed: true }
      );
    }
    removeHabit(habit.id, element);
  } catch (requestError) {
    error.textContent = requestError.message;
  }
}

async function deleteHabit(habit, element) {
  var error = document.getElementById("error");
  error.textContent = "";
  try {
    if (habit.serverId != null) {
      await sendTaskRequest(
        "/api/calendar/tasks/" + encodeURIComponent(habit.serverId),
        "DELETE",
        null
      );
    }
    removeHabit(habit.id, element);
  } catch (requestError) {
    error.textContent = requestError.message;
  }
}


var editingId = null;

function setRadio(group, value) {
  document.querySelectorAll('input[name="' + group + '"]').forEach(function (r) {
    r.checked = (r.value === value);
  });
}

function startEdit(h) {
  editingId = h.id;
  document.getElementById("name").value = h.name;
  document.getElementById("start").value = h.start;
  document.getElementById("end").value = h.end;
  document.getElementById("duration").value = h.duration;
  setRadio("time", h.time);
  document.getElementById("submit-btn").textContent = "Save changes";
  document.getElementById("cancel-btn").hidden = false;
  document.getElementById("error").textContent = "";
  render();
  document.getElementById("habit-form").scrollIntoView({ block: "nearest" });
  document.getElementById("name").focus();
}

function resetForm() {
  editingId = null;
  document.getElementById("name").value = "";
  document.getElementById("start").value = iso(today);
  document.getElementById("end").value = iso(later);
  document.getElementById("duration").value = "30 min";
  setRadio("time", TIMES[0]);
  document.getElementById("submit-btn").textContent = "Create habit";
  document.getElementById("cancel-btn").hidden = true;
  document.getElementById("error").textContent = "";
  render();
}

document.getElementById("cancel-btn").addEventListener("click", resetForm);
document.getElementById("habit-form").addEventListener("submit", async function (e) {
  e.preventDefault();
  var name = document.getElementById("name").value.trim();
  var start = document.getElementById("start").value;
  var end = document.getElementById("end").value;
  var error = document.getElementById("error");

  if (!name) { error.textContent = "Enter a habit name."; return; }
  if (!start || !end) { error.textContent = "Choose a start and end date."; return; }
  if (end < start) { error.textContent = "The end date must be on or after the start date."; return; }
  error.textContent = "";


  var data = {
    name: name,
    start: start,
    end: end,
    duration: document.getElementById("duration").value,
    time: document.querySelector('input[name="time"]:checked').value
    //icon: document.querySelector('input[name="icon"]:checked').value
  };

  try {
    if (editingId != null) {
      var existing = habits.find(function (habit) { return habit.id === editingId; });
      if (existing.serverId != null) {
        await sendTaskRequest(
          "/api/calendar/tasks/" + encodeURIComponent(existing.serverId),
          "PUT",
          taskPayload(data)
        );
        data.id = existing.id;
        data.serverId = existing.serverId;
      } else {
        var savedTask = await sendTaskRequest("/api/calendar/tasks", "POST", taskPayload(data));
        data.id = savedTask.taskId;
        data.serverId = savedTask.taskId;
      }
      habits = habits.map(function (h) {
        return h.id === editingId ? data : h;
      });
      save();
      resetForm();
    } else {
      var createdTask = await sendTaskRequest("/api/calendar/tasks", "POST", taskPayload(data));
      data.id = createdTask.taskId;
      data.serverId = createdTask.taskId;
      habits.push(data);
      save();
      render();
      document.getElementById("name").value = "";
    }
  } catch (requestError) {
    error.textContent = requestError.message;
  }
});
render();