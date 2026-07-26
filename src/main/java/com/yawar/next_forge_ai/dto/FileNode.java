package com.yawar.next_forge_ai.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class FileNode {
    private String path;

    @Override
    public String toString() {
        return path;
    }
}
