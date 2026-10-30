#!/bin/bash
cat PE110_00.part PE110_01.part PE110_02.part > PromptEngine-1.1.0.apk
echo done: $(md5sum PromptEngine-1.1.0.apk)
