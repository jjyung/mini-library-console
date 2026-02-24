const childProcess = require("child_process");
const path = require("path");

const projectRootPath = path.resolve(__dirname, "..");
const apiRootPath = path.join(projectRootPath, "apps", "api", "library-mini-admin-api");
const mavenArguments = process.argv.slice(2);

if (mavenArguments.length === 0) {
  console.error("Missing Maven arguments. Example: npm run dev:api");
  process.exit(1);
}

const isWindowsPlatform = process.platform === "win32";
const command = isWindowsPlatform ? "cmd.exe" : "./mvnw";
const commandArguments = isWindowsPlatform
  ? ["/d", "/s", "/c", "mvnw.cmd", ...mavenArguments]
  : mavenArguments;

const childProcessHandle = childProcess.spawn(command, commandArguments, {
  cwd: apiRootPath,
  stdio: "inherit"
});

childProcessHandle.on("error", (error) => {
  console.error("Failed to start backend process.", error);
  process.exit(1);
});

childProcessHandle.on("exit", (code, signal) => {
  if (signal) {
    process.kill(process.pid, signal);
    return;
  }

  process.exit(code ?? 1);
});
