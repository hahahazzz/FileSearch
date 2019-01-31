@echo off
for /d %%a in (.\*)do (
    for /d %%b in (%%a\*build) do (
        echo del %%b
        rd /s /q %%b
    )
)
echo Done!