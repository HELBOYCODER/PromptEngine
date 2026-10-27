#!/bin/bash
cat PE101_00.part PE101_01.part PE101_02.part > PromptEngine-1.0.1.apk
echo done: $(md5sum PromptEngine-1.0.1.apk)
