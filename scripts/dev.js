const childProcess = require("child_process");
const path = require("path");

const projectRootPath = path.resolve(__dirname, "..");
const webRootPath = path.join(projectRootPath, "apps", "web", "library-mini-admin-web");
const runApiCommandScriptPath = path.join(__dirname, "run-api-command.js");
const npmExecutableName = process.platform === "win32" ? "npm.cmd" : "npm";

const backendProcess = childProcess.spawn(
  process.execPath,
  [runApiCommandScriptPath, "spring-boot:run"],
  {
    cwd: projectRootPath,
    stdio: "inherit"
  }
);

const frontendProcess = childProcess.spawn(
  npmExecutableName,
  ["--prefix", webRootPath, "run", "dev"],
  {
    cwd: projectRootPath,
    stdio: "inherit"
  }
);

const childProcesses = [backendProcess, frontendProcess];
let hasExited = false;

const terminateChildProcesses = () => {
  childProcesses.forEach((childProcessHandle) => {
    if (childProcessHandle.exitCode === null && childProcessHandle.killed === false) {
      childProcessHandle.kill("SIGTERM");
    }
  });
};

const finalizeProcess = (code) => {
  if (hasExited) {
    return;
  }

  hasExited = true;
  terminateChildProcesses();
  process.exit(code);
};

childProcesses.forEach((childProcessHandle, index) => {
  childProcessHandle.on("error", (error) => {
    const processName = index === 0 ? "backend" : "frontend";
    console.error(`Failed to start ${processName} process.`, error);
    finalizeProcess(1);
  });

  childProcessHandle.on("exit", (code) => {
    if (hasExited) {
      return;
    }

    finalizeProcess(code ?? 1);
  });
});

["SIGINT", "SIGTERM"].forEach((signal) => {
  process.on(signal, () => {
    finalizeProcess(0);
  });
});
