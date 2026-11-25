#!/usr/bin/env bash
# Checks the idea.log of a runIde sandbox: the plugin loaded, its startup code ran in an open project,
# and the IDE logged no warning or error that names the plugin.
# The IDE loads most actions only when it first shows them. So before this check, open a file in the sandbox
# and open the plugin popup (Alt+M), otherwise a broken action is not in the log.
#
# Usage: scripts/check-plugin-load.sh path/to/idea.log
set -u

log=${1:?usage: $0 path/to/idea.log}
if [[ ! -f $log ]]; then
	echo "FAIL: no log file at $log"
	exit 1
fi

version=$(sed -n 's/^pluginVersion=//p' "$(dirname "$0")/../gradle.properties")
failed=0

check() { # description, extended regex that a log line must match
	if line=$(grep -m1 -E -- "$2" "$log"); then
		echo "PASS: $1"
		echo "      ${line:0:220}"
	else
		echo "FAIL: $1 (no line matches: $2)"
		failed=1
	fi
}

check "the IDE is a 263 build" ' IDE: .*\(build #[A-Z]+-263\.'
check "the IDE loaded String Manipulation $version" "Loaded custom plugins: .*\\(String Manipulation, ${version//./\\.}\\)"
check "the plugin's startup activity ran" '#osmedile\.intellij\.stringmanip\.ShortcutStartupActivity - Registering '

# A log entry is a timestamped line plus the lines that follow it, such as a stack trace.
# The sandbox directory is also named StringManipulation, so only these markers count as naming the plugin.
errors=$(awk '
	function flush() {
		if (bad_level && entry ~ /osmedile|Plugin to blame: String ?Manipulation|\[Plugin: String Manipulation\]|Plugin .String ?Manipulation|id=String Manipulation|String Manipulation \(/) {
			count++
			print substr(first, 1, 220)
		}
	}
	/^[0-9]{4}-[0-9]{2}-[0-9]{2} / {
		flush()
		first = $0
		entry = $0
		bad_level = ($0 ~ /\] +(WARN|ERROR|SEVERE) +- /)
		next
	}
	{ entry = entry "\n" $0 }
	END { flush() }
' "$log")

if [[ -z $errors ]]; then
	echo "PASS: no warning or error in the log names the plugin"
else
	echo "FAIL: $(printf '%s\n' "$errors" | wc -l | tr -d ' ') log entries at WARN or higher name the plugin:"
	printf '%s\n' "$errors" | head -20 | sed 's/^/      /'
	failed=1
fi

if ((failed)); then
	echo "RESULT: FAIL ($log)"
	exit 1
fi
echo "RESULT: PASS ($log)"
