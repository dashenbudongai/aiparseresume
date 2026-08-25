# resume-cli launcher (Windows PowerShell).
# Usage: .\resume-cli.ps1 parse samples\resume.pdf
# or:   .\resume-cli.ps1 extract --mock samples\resume.pdf
# Auto-discovers a JDK 17+ on the system if JAVA_HOME is not set.
[CmdletBinding()]
param(
    [Parameter(ValueFromRemainingArguments=$true)]
    [string[]]$Args
)

$ErrorActionPreference = 'Stop'

$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$Jar = $null
foreach ($c in @(
    (Join-Path $ScriptDir 'target\resume-cli.jar'),
    (Join-Path $ScriptDir 'resume-cli.jar'),
    (Join-Path (Split-Path -Parent $ScriptDir) 'target\resume-cli.jar')
)) {
    if (Test-Path $c) { $Jar = $c; break }
}
if (-not $Jar) {
    Write-Error "[resume-cli] Cannot find target\resume-cli.jar. Please run 'mvn -DskipTests package' first."
    exit 1
}

$MinMajor = 17

function Get-JdkMajor([string]$JavaHome) {
    $exe = Join-Path $JavaHome 'bin\java.exe'
    if (-not (Test-Path $exe)) { return $null }
    try {
        $psi = New-Object System.Diagnostics.ProcessStartInfo
        $psi.FileName = $exe
        $psi.Arguments = '-XshowSettings:properties --version'
        $psi.RedirectStandardOutput = $true
        $psi.RedirectStandardError = $true
        $psi.UseShellExecute = $false
        $p = [System.Diagnostics.Process]::Start($psi)
        $stdout = $p.StandardOutput.ReadToEnd()
        $stderr = $p.StandardError.ReadToEnd()
        $p.WaitForExit()
    } catch {
        return $null
    }
    $combined = $stdout + "`n" + $stderr
    # Try several property keys (different JDKs expose different ones)
    $line = $null
    foreach ($key in 'java\.runtime\.version', 'java\.version', 'java\.specification\.version') {
        $line = ($combined -split "`n") | Where-Object { $_ -match $key } | Select-Object -First 1
        if ($line) { break }
    }
    if (-not $line) { return 0 }
    # line looks like "    java.runtime.version = 26.0.2+10-55"
    # find the value after the first "=" sign
    $eq = $line.IndexOf('=')
    if ($eq -lt 0) { return 0 }
    $ver = $line.Substring($eq + 1).Trim()
    $major = 0
    [int]::TryParse($ver.Split('.')[0], [ref]$major) | Out-Null
    return $major
}

# 1) JAVA_HOME
$Java = $null
if ($env:JAVA_HOME) {
    $m = Get-JdkMajor $env:JAVA_HOME
    if ($m -and $m -ge $MinMajor) {
        $Java = Join-Path $env:JAVA_HOME 'bin\java.exe'
    }
}

# 2) Auto-discover
if (-not $Java) {
    $bases = @(
        'C:\Program Files\Java',
        'C:\Program Files\Eclipse Adoptium',
        'C:\Program Files\Amazon Corretto',
        'C:\Program Files\Microsoft',
        'C:\Program Files\Zulu',
        'C:\Program Files\BellSoft',
        'C:\Program Files\Semeru'
    )
    $best = 0
    foreach ($base in $bases) {
        if (-not (Test-Path $base)) { continue }
        foreach ($d in (Get-ChildItem -Directory $base -ErrorAction SilentlyContinue)) {
            $exePath = Join-Path $d.FullName 'bin\java.exe'
            if (-not (Test-Path $exePath)) { continue }
            $m = Get-JdkMajor $d.FullName
            if ($m -and $m -ge $MinMajor -and $m -gt $best) {
                $best = $m
                $Java = $exePath
            }
        }
    }
}

# 3) PATH
if (-not $Java) {
    $onPath = (Get-Command java -ErrorAction SilentlyContinue)
    if ($onPath) {
        $Java = $onPath.Source
        Write-Warning "[resume-cli] JAVA_HOME not set and no JDK $MinMajor+ found; using PATH java. May fail if older than JDK $MinMajor."
    } else {
        Write-Error "[resume-cli] Cannot find any java. Please install JDK $MinMajor+ or set JAVA_HOME."
        exit 1
    }
}

& $Java -jar $Jar @Args
exit $LASTEXITCODE
