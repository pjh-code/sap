package com.spring.sap.lombok_guide;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HelloLombok {
    private String hello;
    private int lombok;

    public static void main(String[] args) {
        HelloLombok helloLombok = new HelloLombok();
        helloLombok.setHello("헬로"); // 변수명.setHello("값") 입력
        helloLombok.setLombok(5);

        System.out.println(helloLombok.getHello()); // 변수앞에 get 넣고 값 / set 넣으면 그걸 출력 
        System.out.println(helloLombok.getLombok());
    }
}
