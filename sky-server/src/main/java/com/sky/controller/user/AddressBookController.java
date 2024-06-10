package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.entity.AddressBook;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Slf4j
@Api(tags = "C端-地址簿接口")
public class AddressBookController {
    @Autowired
    private AddressBookService addressBookService;

    private static final Integer DEFAULT_ADDRESS = 1;

    private static final Integer NOT_DEFAULT_ADDRESS = 0;

    /**
     * 查询当前登录用户的所有地址信息
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询当前登录用户的所有地址信息\n")
    public Result<List<AddressBook>> list() {
        log.info("查询当前登录用户的所有地址信息,当前用户id为：{}", BaseContext.getCurrentId());
        //封装用户id
        AddressBook addressBook = AddressBook.builder().userId(BaseContext.getCurrentId()).build();
        //查询当前登录用户的所有地址信息
        List<AddressBook> addressBookList = addressBookService.list(addressBook);
        return Result.success(addressBookList);
    }

    /**
     * 新增地址
     * @return
     */
    @PostMapping
    @ApiOperation("新增地址")
    public Result addAddress(@RequestBody AddressBook addressBook) {
        log.info("新增地址:{}", addressBook);
        addressBookService.addAddress(addressBook);
        return Result.success();
    }

    /**
     * 查询默认地址
     * @return
     */
    @GetMapping("/default")
    @ApiOperation("查询默认地址")
    public Result<AddressBook> getDefault() {
        log.info("查询默认地址");
        //封装查询条件
        AddressBook addressBookC = AddressBook.builder()
                .userId(BaseContext.getCurrentId())
                .isDefault(DEFAULT_ADDRESS)
                .build();
        List<AddressBook> list = addressBookService.list(addressBookC);
        if (list == null || list.isEmpty()) {
            return Result.error("未查询到默认地址！");
        }
        return Result.success(list.get(0));
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询地址")
    public Result<AddressBook> getById(@PathVariable("id") Long id) {
        log.info("根据id查询地址:{}", id);
        AddressBook addressBook = addressBookService.list(AddressBook.builder().id(id).build()).get(0);
        return Result.success(addressBook);
    }

    /**
     * 根据id修改地址
     * @return
     */
    @PutMapping
    @ApiOperation("根据id修改地址")
    public Result updateAddress(@RequestBody AddressBook addressBook) {
        log.info("根据id修改地址:{}", addressBook);
        addressBookService.updateAddress(addressBook);
        return Result.success();
    }

    /**
     * 根据id删除地址
     * @param id
     * @return
     */
    @DeleteMapping
    @ApiOperation("根据id删除地址")
    public Result deleteAddress(@RequestParam("id") Long id) {
        log.info("根据id删除地址:{}", id);
        addressBookService.deleteAddressById(id);
        return Result.success();
    }

    /**
     * 设置默认地址
     * @param addressBook
     * @return
     */
    @PutMapping("/default")
    @ApiOperation("设置默认地址")
    public Result changeDefaultAddress(@RequestBody AddressBook addressBook) {
        log.info("设置默认地址:{}", addressBook);
        addressBookService.changeDefaultAddress(addressBook);
        return Result.success();
    }
}

