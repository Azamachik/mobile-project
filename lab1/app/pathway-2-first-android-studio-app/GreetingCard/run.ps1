<#
.SYNOPSIS
    Собирает Greeting Card, устанавливает на эмулятор и открывает MainActivity.

.DESCRIPTION
    1. Находит Java (встроенную в Android Studio) и Android SDK.
    2. Если нет запущенного эмулятора или подключённого телефона - запускает эмулятор и ждёт загрузки Android.
    3. Собирает и устанавливает приложение (gradlew installDebug).
    4. Открывает MainActivity.

.PARAMETER Avd
    Имя эмулятора, который нужно запустить. По умолчанию medium_phone.

.PARAMETER ColdBoot
    Запустить эмулятор с нуля, без сохранённого состояния. Помогает, если эмулятор завис.
    Действует только если эмулятор ещё не запущен.

.PARAMETER BootTimeoutSec
    Сколько секунд ждать загрузки Android. По умолчанию 300.

.EXAMPLE
    .\run.ps1

.EXAMPLE
    .\run.ps1 -ColdBoot
#>
param(
    [string]$Avd = "medium_phone",
    [switch]$ColdBoot,
    [int]$BootTimeoutSec = 300
)

$AppId     = "com.example.greetingcard"
$Activity  = ".MainActivity"
$StudioJbr = "C:\Program Files\Android\Android Studio\jbr"

function Write-Step([string]$Text) {
    Write-Host "==> $Text" -ForegroundColor Cyan
}

function Exit-WithError([string]$Text) {
    Write-Host "ОШИБКА: $Text" -ForegroundColor Red
    exit 1
}

function Get-SdkPath {
    $props = Join-Path $PSScriptRoot "local.properties"
    if (Test-Path $props) {
        $match = Select-String -Path $props -Pattern '^\s*sdk\.dir\s*=\s*(.+)$' | Select-Object -First 1
        if ($match) {
            # В local.properties путь экранирован: C\:\\Users\\...
            return ($match.Matches[0].Groups[1].Value.Trim() -replace '\\:', ':' -replace '\\\\', '\')
        }
    }
    if ($env:ANDROID_HOME) { return $env:ANDROID_HOME }
    return "$env:LOCALAPPDATA\Android\Sdk"
}

# Список устройств из "adb devices": серийный номер и состояние (device / offline / unauthorized)
function Get-Devices {
    $list = @()
    foreach ($line in (& $Adb devices 2>$null)) {
        if ($line -match '^(\S+)\s+(device|offline|unauthorized)\s*$') {
            $list += [pscustomobject]@{ Serial = $Matches[1]; State = $Matches[2] }
        }
    }
    return $list
}

function Test-Booted([string]$Serial) {
    $value = & $Adb -s $Serial shell getprop sys.boot_completed 2>$null
    return ("$value".Trim() -eq "1")
}

function Wait-ForDevice($EmulatorProcess) {
    $deadline = (Get-Date).AddSeconds($BootTimeoutSec)
    while ((Get-Date) -lt $deadline) {
        $devices = @(Get-Devices)
        foreach ($d in $devices) {
            if ($d.State -eq "device" -and (Test-Booted $d.Serial)) {
                Write-Host ""
                return $d.Serial
            }
        }
        if ($EmulatorProcess -and $EmulatorProcess.HasExited -and $devices.Count -eq 0) {
            Write-Host ""
            Exit-WithError "Эмулятор закрылся во время запуска. Попробуй: .\run.ps1 -ColdBoot"
        }
        Write-Host "." -NoNewline
        Start-Sleep -Seconds 3
    }
    Write-Host ""
    Exit-WithError "Устройство не загрузилось за $BootTimeoutSec сек. Если подключён телефон - разреши на нём отладку по USB."
}

$stopwatch = [Diagnostics.Stopwatch]::StartNew()

# 1. Java и Android SDK
if (-not $env:JAVA_HOME) {
    if (-not (Test-Path "$StudioJbr\bin\java.exe")) {
        Exit-WithError "Не найдена Java. Установи Android Studio или задай переменную JAVA_HOME."
    }
    $env:JAVA_HOME = $StudioJbr
}

$Sdk      = Get-SdkPath
$Adb      = Join-Path $Sdk "platform-tools\adb.exe"
$Emulator = Join-Path $Sdk "emulator\emulator.exe"
if (-not (Test-Path $Adb)) { Exit-WithError "Не найден adb: $Adb" }

# 2. Устройство: используем уже запущенное или запускаем эмулятор
Write-Step "Ищу запущенный эмулятор или телефон"
$emulatorProcess = $null
if (@(Get-Devices).Count -eq 0) {
    if (-not (Test-Path $Emulator)) { Exit-WithError "Не найден эмулятор: $Emulator" }

    $avds = @(& $Emulator -list-avds 2>$null | ForEach-Object { $_.Trim() })
    if ($avds -notcontains $Avd) {
        $available = if ($avds.Count -gt 0) { $avds -join ", " } else { "нет ни одного" }
        Exit-WithError "Эмулятор '$Avd' не найден. Доступные: $available. Создай его в Android Studio: Device Manager."
    }

    Write-Step "Запускаю эмулятор $Avd"
    $emuArgs = @("-avd", $Avd)
    if ($ColdBoot) { $emuArgs += "-no-snapshot-load" }
    $emulatorProcess = Start-Process -FilePath $Emulator -ArgumentList $emuArgs -PassThru
}

Write-Step "Жду, пока Android загрузится"
$serial = Wait-ForDevice $emulatorProcess
# adb и Gradle будут работать именно с этим устройством
$env:ANDROID_SERIAL = $serial
Write-Host "    Устройство: $serial" -ForegroundColor Green

# 3. Сборка и установка
Write-Step "Собираю и устанавливаю приложение (gradlew installDebug)"
& (Join-Path $PSScriptRoot "gradlew.bat") installDebug
if ($LASTEXITCODE -ne 0) { Exit-WithError "Сборка или установка не удалась, смотри вывод выше." }

# 4. Запуск MainActivity (-S перезапускает приложение, если оно уже открыто)
Write-Step "Открываю $AppId/$Activity"
$output = & $Adb shell am start -S -n "$AppId/$Activity"
$output | ForEach-Object { Write-Host "    $_" }
if ($LASTEXITCODE -ne 0 -or ($output -match "Error")) { Exit-WithError "Не удалось открыть приложение." }

Write-Host ("Готово! Приложение открыто на {0} за {1:N0} сек." -f $serial, $stopwatch.Elapsed.TotalSeconds) -ForegroundColor Green
