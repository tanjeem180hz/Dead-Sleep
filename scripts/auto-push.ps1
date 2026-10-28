param (
    [int]$IntervalSeconds = 15,
    [string]$Remote = "origin",
    [string]$Branch = "main",
    [switch]$Once
)

# Move to the repository root directory
$repoRoot = Split-Path -Parent $PSScriptRoot
if (-not $repoRoot -or -not (Test-Path "$repoRoot\.git")) {
    $repoRoot = (Get-Item .).FullName
}
Set-Location $repoRoot

Write-Host "=====================================================" -ForegroundColor Cyan
Write-Host " Dead Sleep - Git Auto-Sync Monitor" -ForegroundColor Cyan
Write-Host " Repository : $repoRoot" -ForegroundColor DarkGray
Write-Host " Target     : $Remote/$Branch" -ForegroundColor DarkGray
Write-Host " Interval   : Every $IntervalSeconds seconds" -ForegroundColor DarkGray
Write-Host "=====================================================" -ForegroundColor Cyan

function Sync-Git {
    try {
        # Check if there are changes (modified, untracked, deleted)
        $status = git status --porcelain
        $hasWorkingChanges = -not [string]::IsNullOrWhiteSpace($status)

        # Check if there are local commits that have not been pushed yet
        $unpushed = git log "$Remote/$Branch..HEAD" --oneline 2>$null
        $hasUnpushedCommits = -not [string]::IsNullOrWhiteSpace($unpushed)

        if ($hasWorkingChanges -or $hasUnpushedCommits) {
            $timestamp = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
            Write-Host "[$timestamp] Changes detected! Synchronizing..." -ForegroundColor Yellow

            if ($hasWorkingChanges) {
                git add -A
                git commit -m "Auto-update: $timestamp"
            }

            # Pull remote updates if any
            git pull --rebase $Remote $Branch 2>$null

            # Push all commits to remote
            $pushOutput = git push $Remote $Branch 2>&1
            if ($LASTEXITCODE -eq 0) {
                Write-Host "[$timestamp] Successfully synced and pushed to $Remote/$Branch!" -ForegroundColor Green
            } else {
                Write-Host "[$timestamp] Push warning/error: $pushOutput" -ForegroundColor Red
            }
        }
    } catch {
        Write-Host "[$((Get-Date).ToString('yyyy-MM-dd HH:mm:ss'))] Error during sync: $_" -ForegroundColor Red
    }
}

if ($Once) {
    Sync-Git
    exit 0
}

Write-Host "Watching for file changes... (Press Ctrl+C to stop)" -ForegroundColor Green

while ($true) {
    Sync-Git
    Start-Sleep -Seconds $IntervalSeconds
}
