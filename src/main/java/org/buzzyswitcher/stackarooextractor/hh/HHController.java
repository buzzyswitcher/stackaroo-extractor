package org.buzzyswitcher.stackarooextractor.hh;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class HHController {

    HHInteractor interactor;

    public HHController(HHInteractor interactor) {
        this.interactor = interactor;
    }

    @GetMapping("/hh")
    public Set<String> test() {
        Set<String> ids = interactor.getIds();
        interactor.downloadVacancies(ids);
        return ids;
    }

    @GetMapping("/testbool")
    public Boolean test_test() {
        return interactor.test();
    }
}
