package org.example.mail;

import lombok.Builder;
import lombok.Data;

@Data
public class Request {
    private String name;
    public Email email;
    public Content content;
}
